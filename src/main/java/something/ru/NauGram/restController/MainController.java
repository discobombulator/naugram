package something.ru.NauGram.restController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.LocaleResolver;
import something.ru.NauGram.dto.ChatUpdateDTO;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.UsersProfile;
import something.ru.NauGram.service.ChatLastReadService;
import something.ru.NauGram.service.ChatService;
import something.ru.NauGram.service.UserProfileService;
import something.ru.NauGram.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Controller
@AllArgsConstructor
public class MainController {
    private final UserService userService;
    private final UserProfileService userProfileService;
    private final ChatLastReadService chatLastReadService;
    private final ChatService chatService;

    private final LocaleResolver localeResolver;

    /**
     * Отображает главную страницу приложения.
     *
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона главной страницы
     */
    @GetMapping("/")
    public String showMainPage(Model model,
                               Authentication authentication,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        User currentUser = userService.findByEmail(authentication.getName());

        if (currentUser == null) {
            return "redirect:/login";
        }

        String currentLanguage = "ru";

        if (currentUser.getLanguage() != null && !currentUser.getLanguage().isBlank()) {
            currentLanguage = currentUser.getLanguage();
        }

        localeResolver.setLocale(
                request,
                response,
                Locale.forLanguageTag(currentLanguage)
        );

        Optional<UsersProfile> currentUserProfileOptional = userProfileService.findByUser(currentUser);
        UsersProfile currentUserProfile = currentUserProfileOptional.orElse(null);

        String firstName = "";
        String lastName = "";

        if (currentUserProfile != null && currentUserProfile.getRealName() != null) {
            String[] parts = currentUserProfile.getRealName().trim().split("\\s+", 2);
            firstName = parts.length > 0 ? parts[0] : "";
            lastName = parts.length > 1 ? parts[1] : "";
        }

        List<Chat> chats = chatService.getCurrentUserChats(currentUser);

        List<ChatUpdateDTO> initLastReadMessages = new ArrayList<>();
        for (Chat chat : chats) {
            initLastReadMessages.add(
                    new ChatUpdateDTO(
                            chat.getId(),
                            chatLastReadService.getLastMessage(currentUser, chat),
                            chatLastReadService.getUnreadMessages(currentUser, chat)
                    )
            );
        }
        log.info("{}", initLastReadMessages);
        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("currentLanguage", currentLanguage);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("chats", chats);
        model.addAttribute("profile", currentUserProfile);
        model.addAttribute("initialChatLastMessages", initLastReadMessages);

        return "mainPage";
    }

}
