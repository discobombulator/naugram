package something.ru.NauGram.restController;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.UsersProfile;
import something.ru.NauGram.service.ChatService;
import something.ru.NauGram.service.UserProfileService;
import something.ru.NauGram.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.LocaleResolver;
import something.ru.NauGram.model.Chat;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Controller
public class MainController {
    private final UserService userService;
    private final UserProfileService userProfileService;

    private final ChatService chatService;

    private final LocaleResolver localeResolver;

    public MainController(UserService userService,
                          UserProfileService userProfileService, ChatService chatService,
                          LocaleResolver localeResolver) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.chatService = chatService;
        this.localeResolver = localeResolver;
    }

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

        if (currentUser != null && currentUser.getLanguage() != null && !currentUser.getLanguage().isBlank()) {
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

        model.addAttribute("firstName", firstName);
        model.addAttribute("lastName", lastName);
        model.addAttribute("currentLanguage", currentLanguage);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("chats", chats);
        model.addAttribute("profile", currentUserProfile);

        return "mainPage";
    }

}
