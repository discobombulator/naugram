package something.ru.NauGram.restController;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import something.ru.NauGram.model.User;
import something.ru.NauGram.service.MediaStorageService;
import something.ru.NauGram.service.UserProfileService;
import something.ru.NauGram.service.UserService;

/**
 * Контроллер для загрузки пользовательских медиафайлов.
 */
@Controller
public class MediaController {

    private final UserService userService;
    private final UserProfileService userProfileService;
    private final MediaStorageService mediaStorageService;

    public MediaController(UserService userService,
                           UserProfileService userProfileService,
                           MediaStorageService mediaStorageService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.mediaStorageService = mediaStorageService;
    }

    /**
     * Загружает и сохраняет аватар текущего пользователя.
     *
     * @param avatar файл аватара
     * @param authentication данные текущей авторизации
     * @param redirectAttributes flash-атрибуты для уведомлений
     * @return редирект на главную страницу
     */
    @PostMapping("/profile/avatar")
    public String uploadAvatar(@RequestParam("avatar") MultipartFile avatar,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(authentication.getName());

            if (currentUser == null) {
                throw new RuntimeException("Пользователь не найден");
            }

            String avatarPath = mediaStorageService.saveUserAvatar(currentUser, avatar);
            userProfileService.updateAvatar(currentUser, avatarPath);

            redirectAttributes.addFlashAttribute("success", "Аватар обновлён");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/";
    }
}