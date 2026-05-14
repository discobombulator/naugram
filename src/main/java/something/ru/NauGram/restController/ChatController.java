package something.ru.NauGram.restController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import something.ru.NauGram.dto.MessageDTO;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.Message;
import something.ru.NauGram.model.User;
import something.ru.NauGram.service.ChatParticipantService;
import something.ru.NauGram.service.ChatService;
import something.ru.NauGram.service.MessageService;
import something.ru.NauGram.service.UserService;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final ChatParticipantService chatParticipantService;
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/chats")
    public String chatsPage(Model model) {

        User currentUser = userService.getCurrentUser();

        List<Chat> chats =
                chatService.getCurrentUserChats(currentUser);

        model.addAttribute("chats", chats);
        model.addAttribute("chatCount", chats.size());

        return "chat";
    }

    @GetMapping("/chats/{chatId}")
    public String chatPage(
            @PathVariable Long chatId,
            Model model
    ) {

        User currentUser = userService.getCurrentUser();

        boolean allowed = chatParticipantService.isParticipant(chatId, currentUser.getId());

        if (!allowed) {
            throw new AccessDeniedException(
                    "You do not have access to this chat"
            );
        }

        Chat chat = chatService.getChat(chatId);

        List<Chat> chats = chatService.getCurrentUserChats(currentUser);

        model.addAttribute("selectedChat", chat);
        model.addAttribute("chats", chats);
        model.addAttribute("chatCount", chats.size());
        model.addAttribute("messages", messageService.getInitialMessages(chat.getId()));

        return "chat";
    }

    @Transactional
    @MessageMapping("/chat.send")
    public void sendMessage(
            MessageDTO dto,
            Principal principal
    ) {

        if (principal == null) {
            throw new RuntimeException("Unauthorized");
        }
        User sender = userService.findByUsername(principal.getName());

        boolean allowed = chatParticipantService.isParticipant(dto.getChatId(), sender.getId());

        if (!allowed) {
            throw new AccessDeniedException(
                    "You are not participant of this chat"
            );
        }
        Chat chat = chatService.getChat(dto.getChatId());
        Message savedMessage = messageService.saveMessage(chat, sender, null, dto.getText());
        MessageDTO response = savedMessage.toMessageDTO();
        List<User> users = chatService.getChatParticipants(chat.getId());

        for (User user : users) {
            messagingTemplate.convertAndSendToUser(
                    user.getUsername(),
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
}