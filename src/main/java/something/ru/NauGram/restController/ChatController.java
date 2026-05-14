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

    /**
     * Отображает страницу со списком всех чатов текущего пользователя.
     * Добавляет в модель список чатов и их количество для отображения на странице.
     *
     * @param model объект модели для передачи данных в представление
     * @return имя шаблона представления "chat"
     */
    @GetMapping("/chats")
    public String chatsPage(Model model) {

        User currentUser = userService.getCurrentUser();

        List<Chat> chats =
                chatService.getCurrentUserChats(currentUser);

        model.addAttribute("chats", chats);
        model.addAttribute("chatCount", chats.size());

        return "chat";
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