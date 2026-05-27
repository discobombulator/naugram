package something.ru.NauGram.restController;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import something.ru.NauGram.model.User;
import something.ru.NauGram.service.UserService;

/**
 * Контроллер для обработки изменений пользовательских настроек.
 *
 * <p>Отвечает за сохранение настроек, отправленных из формы настроек:
 * выбранного языка интерфейса, нового пароля и статуса двухфакторной
 * аутентификации.</p>
 */
@Controller
public class UserSettingsChangeController {

    private final UserService userService;

    /**
     * Создаёт контроллер изменения настроек пользователя.
     *
     * @param userService сервис для работы с пользователями
     */
    public UserSettingsChangeController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Обрабатывает отправку формы настроек пользователя.
     *
     * <p>Метод получает текущего авторизованного пользователя, сохраняет выбранную
     * локализацию, при необходимости обновляет пароль и включает или выключает
     * двухфакторную аутентификацию.</p>
     *
     * <p>После успешного сохранения пользователь перенаправляется на главную
     * страницу с параметром {@code lang}, чтобы интерфейс сразу отобразился
     * на выбранном языке.</p>
     *
     * @param oldPassword текущий пароль пользователя, может отсутствовать
     * @param newPassword новый пароль пользователя, может отсутствовать
     * @param faStatus статус двухфакторной аутентификации
     * @param language выбранный язык интерфейса
     * @param authentication объект аутентификации текущего пользователя
     * @param redirectAttributes атрибуты для передачи flash-сообщений после редиректа
     * @return редирект на главную страницу приложения
     */
    @PostMapping("/save-settings")
    public String saveSettings(@RequestParam(required = false) String oldPassword,
                               @RequestParam(required = false) String newPassword,
                               @RequestParam(required = false, defaultValue = "false") boolean faStatus,
                               @RequestParam(required = false, defaultValue = "ru") String language,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName());

            if (user == null) {
                throw new RuntimeException("Пользователь не найден");
            }

            userService.changeLocalization(user, language);
            userService.changeUserPassword(user, oldPassword, newPassword);
            userService.change2FaStatus(user, faStatus);

            redirectAttributes.addFlashAttribute("success", "Настройки обновлены");

            return "redirect:/?lang=" + language;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/";
    }
}