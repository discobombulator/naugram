package something.ru.NauGram.restController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Контроллер страницы просмотра медиафайлов.
 */
@Controller
public class MediaViewerController {

    /**
     * Открывает страницу просмотра изображения или видео.
     *
     * @param url ссылка на медиафайл
     * @param type тип медиа: image или video
     * @param back ссылка для возврата назад
     * @param model модель страницы
     * @return шаблон mediaViewer
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