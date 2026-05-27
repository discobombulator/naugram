package something.ru.NauGram.restController;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import something.ru.NauGram.model.User;
import something.ru.NauGram.service.UserProfileService;
import something.ru.NauGram.service.UserService;

/**
 * Контроллер для обработки изменений профиля пользователя.
 *
 * <p>Принимает данные из формы редактирования профиля, определяет текущего
 * авторизованного пользователя и передаёт обновлённые данные в сервис профиля.</p>
 */
@Controller
public class ProfileChangeController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    /**
     * Создаёт контроллер изменения профиля.
     *
     * @param userService сервис для работы с пользователями
     * @param userProfileService сервис для работы с профилями пользователей
     */
    public ProfileChangeController(UserService userService,
                                   UserProfileService userProfileService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
    }

    /**
     * Обрабатывает отправку формы редактирования профиля.
     *
     * <p>Метод обновляет имя, фамилию, тег пользователя, описание профиля
     * и дату рождения. В случае успешного обновления добавляет flash-сообщение
     * об успехе, а при ошибке — flash-сообщение с текстом ошибки.</p>
     *
     * @param firstName новое имя пользователя
     * @param lastName новая фамилия пользователя
     * @param username новый тег пользователя
     * @param bio новое описание профиля
     * @param birthDate новая дата рождения в строковом формате, может отсутствовать
     * @param authentication объект аутентификации текущего пользователя
     * @param redirectAttributes атрибуты для передачи flash-сообщений после редиректа
     * @return редирект на главную страницу
     */
    @PostMapping("/save-profile")
    public String saveProfile(@RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String username,
                              @RequestParam String bio,
                              @RequestParam(required = false) String birthDate,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByEmail(authentication.getName());

            userProfileService.updateProfile(
                    currentUser,
                    firstName,
                    lastName,
                    username,
                    bio,
                    birthDate
            );

            redirectAttributes.addFlashAttribute("success", "Профиль обновлён");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/";
    }
}