package something.ru.NauGram.restController;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.User;
import something.ru.NauGram.service.ChatService;
import something.ru.NauGram.service.UserService;

import java.util.List;

@Slf4j
@Controller
public class ChatController {
    private final ChatService chatService;
    private final UserService userService;


    @Autowired
    public ChatController(ChatService chatService, UserService userService) {
        this.chatService = chatService;
        this.userService = userService;
    }

    /**
     * Main chats page - loads the chat list
     */
    @GetMapping("/chats")
    public String chatsPage(Model model) {
        log.info("Loading chats page");

        try {
            User user = userService.getCurrentUser();
            List<Chat> chats = chatService.getCurrentUserChats(user);
            log.info("chats are - {}", chats);
            model.addAttribute("chats", chats);
            model.addAttribute("chatCount", chats.size());
//            model.addAttribute("chatCount", chats.size());

        } catch (Exception e) {
            log.error("Error loading chats", e);
            model.addAttribute("chats", List.of());
            model.addAttribute("error", "Failed to load chats");
        }

        return "chat"; // This will look for src/main/resources/templates/chats.html
    }

    /**
     * Specific chat page
     */
    @GetMapping("/chat/{chatId}")
    public String chatPage(@PathVariable Long chatId, Model model) {
        log.info("Loading chat page for chat ID: {}", chatId);

        // You can add chat-specific data here
        model.addAttribute("chatId", chatId);

        return "chat"; // This will look for src/main/resources/templates/chat.html
    }

}
