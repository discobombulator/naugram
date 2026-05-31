package something.ru.NauGram.restController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Контроллер страницы просмотра медиафайлов.
 *
 * <p>Открывает отдельную страницу для просмотра изображения или видео
 * из чата с возможностью вернуться обратно в чат.</p>
 */
@Controller
public class MediaViewerController {

    /**
     * Открывает страницу просмотра медиафайла.
     *
     * @param url публичный путь к медиафайлу
     * @param type тип медиафайла: {@code image} или {@code video}
     * @param back ссылка для возврата на предыдущую страницу
     * @param model модель страницы
     * @return имя шаблона {@code mediaViewer}
     */
    @GetMapping("/media-viewer")
    public String mediaViewer(@RequestParam String url,
                              @RequestParam String type,
                              @RequestParam String back,
                              Model model) {
        model.addAttribute("url", url);
        model.addAttribute("type", type);
        model.addAttribute("back", back);

        return "mediaViewer";
    }
}