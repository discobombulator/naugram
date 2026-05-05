package something.ru.NauGram.handler;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import something.ru.NauGram.model.User;
import something.ru.NauGram.service.UserService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;


public class MessageHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(MessageHandler.class);

    private static final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private static final Map<WebSocketSession, SessionInfo> sessionInfo = new ConcurrentHashMap<>();

    private final UserService userService;

    public MessageHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Connection establicshtajlk connected");
        String path = Objects.requireNonNull(session.getUri()).getPath();
        String roomId = extractRoomId(path);

        String username = extractAuthenticatedUsername(session);

        if (roomId == null || username == null) {
            log.warn("Connection rejected - roomId: {}, username: {}", roomId, username);
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        log.info("User '{}' connected to room '{}'", username, roomId);

        rooms.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
        sessionInfo.put(session, new SessionInfo(roomId, username));

        // Store user info in session attributes
        session.getAttributes().put("username", username);

        // Get full user object if needed
        User user = getAuthenticatedUser(session);
        if (user != null) {
            session.getAttributes().put("userId", user.getId());
            session.getAttributes().put("userEmail", user.getEmail());
        }

        broadcastToRoom(roomId, createSystemMessage(username + " joined the room"), session);
        broadcastUserList(roomId);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session,
                                     @NonNull TextMessage message) {
        SessionInfo info = sessionInfo.get(session);
        if (info == null) return;

        String content = message.getPayload();

        try {
            JsonNode node = new ObjectMapper().readTree(content);
            if (node.has("content")) {
                content = node.get("content").asString();
            }
        } catch (Exception e) {
            // Not JSON, use as is
        }

        broadcastToRoom(info.roomId, createUserMessage(info.username, content), null);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        SessionInfo info = sessionInfo.remove(session);
        if (info != null) {
            Set<WebSocketSession> roomSessions = rooms.get(info.roomId);
            if (roomSessions != null) {
                roomSessions.remove(session);
                if (roomSessions.isEmpty()) {
                    rooms.remove(info.roomId);
                }
            }

            broadcastToRoom(info.roomId, createSystemMessage(info.username + " left the room"), null);
            broadcastUserList(info.roomId);
        }
    }

    /**
     * Extracts authenticated username from WebSocket session
     * Uses multiple fallback methods to ensure we get the real user
     */
    private String extractAuthenticatedUsername(WebSocketSession session) {
        // Method 1: From session attributes (set by handshake interceptor)
        Object usernameAttr = session.getAttributes().get("username");
        if (usernameAttr != null) {
            return usernameAttr.toString();
        }

        // Method 2: From WebSocket session principal
        Principal principal = session.getPrincipal();
        if (principal != null) {
            String username = principal.getName();
            log.debug("Principal found: {}", username);
            // Handle email-based usernames
            if (username.contains("@")) {
                return username.substring(0, username.indexOf("@"));
            }
            return username;
        }

        // Method 3: From SecurityContextHolder
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            if (auth.getPrincipal() instanceof User user) {
                return user.getUsername();
            }
            return auth.getName();
        }

        return null;
    }


//    /**
//     * Fallback method to extract username from URL query parameters
//     * Only used if user is not authenticated
//     */
//    private String extractUsernameFromQuery(WebSocketSession session) {
//        String query = Objects.requireNonNull(session.getUri()).getQuery();
//        if (query != null) {
//            for (String param : query.split("&")) {
//                String[] pair = param.split("=");
//                if (pair.length == 2 && "username".equals(pair[0])) {
//                    return URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
//                }
//            }
//        }
//        return "Anonymous-" + session.getId().substring(0, 4);
//    }

    /**
     * Gets the full User domain object from WebSocket session
     * Useful if you need more than just the username
     */
    private User getAuthenticatedUser(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal != null) {
            return userService.findByEmail(principal.getName());
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            if (auth.getPrincipal() instanceof User user) {
                return user;
            }
            return userService.findByEmail(auth.getName());
        }

        return null;
    }

    private String extractRoomId(String path) {
        // Expected path: /chat/{roomId}
        String[] parts = path.split("/");
        return parts.length > 2 ? parts[2] : null;
    }

    private String createSystemMessage(String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode message = mapper.createObjectNode();
            message.put("type", "system");
            message.put("content", content);
            message.put("timestamp", System.currentTimeMillis());
            return mapper.writeValueAsString(message);
        } catch (Exception e) {
            return content;
        }
    }

    private String createUserMessage(String username, String content) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode message = mapper.createObjectNode();
            message.put("type", "message");
            message.put("username", username);
            message.put("content", content);
            message.put("timestamp", System.currentTimeMillis());
            return mapper.writeValueAsString(message);
        } catch (Exception e) {
            return username + ": " + content;
        }
    }

    private void broadcastToRoom(String roomId, String message, WebSocketSession excludeSession) {
        Set<WebSocketSession> roomSessions = rooms.get(roomId);
        if (roomSessions != null) {
            TextMessage textMessage = new TextMessage(message);
            for (WebSocketSession session : roomSessions) {
                if (session.isOpen() && !session.equals(excludeSession)) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void broadcastUserList(String roomId) {
        Set<WebSocketSession> roomSessions = rooms.get(roomId);
        if (roomSessions != null) {
            List<String> usernames = roomSessions.stream()
                    .map(sessionInfo::get)
                    .filter(Objects::nonNull)
                    .map(info -> info.username)
                    .collect(Collectors.toList());

            String userList = "ONLINE_USERS:" + String.join(",", usernames);
            TextMessage textMessage = new TextMessage(userList);

            for (WebSocketSession session : roomSessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static class SessionInfo {
        String roomId;
        String username;

        SessionInfo(String roomId, String username) {
            this.roomId = roomId;
            this.username = username;
        }
    }
}