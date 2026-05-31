package something.ru.NauGram.restController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import something.ru.NauGram.dto.CreateChatDTO;
import something.ru.NauGram.dto.MessageDTO;
import something.ru.NauGram.dto.UserSearchDTO;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.Message;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.UsersProfile;
import something.ru.NauGram.service.*;

import java.security.Principal;
import java.util.List;

/**
 * Контроллер для управления чатами и отправки сообщений.
 * Обрабатывает HTTP-запросы для отображения страниц чатов
 * и WebSocket-сообщения для отправки сообщений в реальном времени.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final ChatParticipantService chatParticipantService;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserProfileService userProfileService;
    private final MediaStorageService mediaStorageService;

    /**
     * Отображает страницу со списком всех чатов текущего пользователя.
     * Добавляет в модель список чатов и их количество для отображения на странице.
     *
     * @return имя шаблона представления "chat"
     */
    @GetMapping("/chats")
    public String chatsPage() {
        return "redirect:/";
    }

    /**
     * Отображает страницу конкретного чата с сообщениями.
     * Проверяет права доступа текущего пользователя к запрашиваемому чату.
     * Если пользователь не является участником чата, выбрасывается исключение.
     * Добавляет в модель данные выбранного чата, список всех чатов пользователя,
     * их количество и начальные сообщения выбранного чата.
     *
     * @param chatId идентификатор запрашиваемого чата
     * @param model  объект модели для передачи данных в представление
     * @return имя шаблона представления "chat"
     * @throws AccessDeniedException если текущий пользователь не является участником чата
     */
    @GetMapping("/chats/{chatId}")
    public String chatPage(@PathVariable Long chatId, Model model) {
        User currentUser = userService.getCurrentUser();

        boolean allowed = chatParticipantService.isParticipant(chatId, currentUser.getId());

        if (!allowed) {
            throw new AccessDeniedException("You do not have access to this chat");
        }

        Chat chat = chatService.getChat(chatId);
        User companion = chatService.getCompanion(chat, currentUser);

        UsersProfile companionProfile = companion != null
                ? userProfileService.findByUser(companion).orElse(null)
                : null;

        model.addAttribute("companion", companion);
        model.addAttribute("companionProfile", companionProfile);

        model.addAttribute("selectedChat", chat);
        model.addAttribute("messages", messageService.getInitialMessages(chat.getId()));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("currentUsername", currentUser.getUsername());

        return "chat";
    }

    /**
     * Обрабатывает WebSocket-сообщение для отправки сообщения в чат.
     * Проверяет авторизацию отправителя и его права на участие в чате.
     * Сохраняет сообщение в базе данных и рассылает его всем участникам чата
     * через WebSocket-каналы в реальном времени.
     * Метод выполняется в транзакционном контексте.
     *
     * @param dto       объект с данными сообщения (идентификатор чата и текст)
     * @param principal объект аутентификации текущего пользователя WebSocket-соединения
     * @throws RuntimeException      если пользователь не авторизован (principal равен null)
     * @throws AccessDeniedException если отправитель не является участником указанного чата
     */
    @Transactional
    @MessageMapping("/chat.send")
    public void sendMessage(MessageDTO dto, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }

        User sender = userService.findByEmail(principal.getName());

        if (sender == null) {
            sender = userService.findByEmail(principal.getName());
        }

        if (sender == null) {
            throw new RuntimeException("Пользователь не найден: " + principal.getName());
        }

        boolean allowed = chatParticipantService.isParticipant(dto.getChatId(), sender.getId());

        if (!allowed) {
            throw new AccessDeniedException("You are not participant of this chat");
        }

        Chat chat = chatService.getChat(dto.getChatId());

        Message savedMessage = messageService.saveMessage(chat, sender, null, dto.getText());
        MessageDTO response = messageService.convertToDto(savedMessage);

        List<User> users = chatService.getChatParticipants(chat.getId());

        for (User user : users) {
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/queue/chat/" + chat.getId(),
                    response
            );
        }

        log.info(
                "Message \"{}\" sent to chat {} by {}",
                response.getText(),
                chat.getId(),
                sender.getUsername()
        );
    }

    /**
     * Отображает страницу создания нового чата.
     *
     * @param model объект модели для передачи данных в представление
     * @return имя HTML-шаблона страницы создания чата
     */
    @GetMapping("/chats/create")
    public String createChat(Model model) {
        return "chatCreation";
    }

    /**
     * Выполняет поиск пользователей по имени пользователя.
     *
     * <p>Возвращает список пользователей, чьи имена содержат
     * указанную строку поиска.</p>
     *
     * @param query строка поиска по имени пользователя
     * @return HTTP-ответ со списком найденных пользователей
     */
    @GetMapping("/api/users/search")
    public ResponseEntity<List<UserSearchDTO>> searchUsers(
            @RequestParam String query
    ) {

        List<UserSearchDTO> users = userService.searchUsers(query);

        return ResponseEntity.ok(users);
    }

    /**
     * Создаёт новый чат.
     *
     * <p>Создаёт чат с указанным названием, описанием и списком
     * участников. Текущий авторизованный пользователь автоматически
     * становится участником создаваемого чата.</p>
     *
     * @param request DTO с данными нового чата
     * @return HTTP-ответ с идентификатором созданного чата
     */
    @PostMapping("/api/chats/create")
    public ResponseEntity<Long> createChat(@RequestBody CreateChatDTO request) {

        Long response =
                chatService.createChat(request, userService.getCurrentUser());

        return ResponseEntity.ok(response);
    }

    /**
     * Загружает медиафайлы в чат и создаёт одно сообщение с вложениями.
     *
     * <p>Метод принимает список файлов и необязательную текстовую подпись.
     * Каждый файл сохраняется на диск, после чего создаётся сообщение,
     * связанное с этими медиафайлами. После сохранения сообщение рассылается
     * всем участникам чата через WebSocket.</p>
     *
     * @param chatId идентификатор чата
     * @param files список загружаемых файлов
     * @param text текстовая подпись к медиа, может быть пустой
     * @return HTTP-ответ с DTO созданного сообщения или текстом ошибки
     */
    @PostMapping("/chats/{chatId}/media")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> uploadChatMedia(@PathVariable Long chatId,
                                             @RequestParam(value = "files", required = false) List<MultipartFile> files,
                                             @RequestParam(value = "text", required = false) String text) {
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        boolean allowed = chatParticipantService.isParticipant(chatId, currentUser.getId());

        if (!allowed) {
            return ResponseEntity.status(403).body("You are not participant of this chat");
        }

        if ((text == null || text.isBlank()) && (files == null || files.isEmpty())) {
            return ResponseEntity.badRequest().body("Сообщение не может быть пустым");
        }

        Chat chat = chatService.getChat(chatId);

        List<MultipartFile> safeFiles = files == null ? List.of() : files;

        List<String> mediaPaths = safeFiles.stream()
                .map(file -> mediaStorageService.saveChatMedia(chatId, file))
                .toList();

        Message savedMessage = messageService.saveMediaMessage(
                chat,
                currentUser,
                text,
                mediaPaths,
                safeFiles
        );

        MessageDTO response = savedMessage.toMessageDTO();

        List<User> users = chatService.getChatParticipants(chat.getId());

        for (User user : users) {
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/queue/chat/" + chat.getId(),
                    response
            );
        }

        return ResponseEntity.ok(response);
    }
}