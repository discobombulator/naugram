package something.ru.NauGram.restController;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public class MainController {

    /**
     * Отображает главную страницу приложения.
     *
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона главной страницы
     */
    @GetMapping("/")
    public String showMainPage(Model model) {
        return "mainPage";
    }

    /**
     * Отображает страницу настроек пользователя.
     *
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона страницы настроек
     */
    @GetMapping("/settings")
    public String showSettingsPage(Model model) {
        return "settings";
    }

}
