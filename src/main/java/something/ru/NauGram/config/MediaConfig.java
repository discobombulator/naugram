package something.ru.NauGram.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Конфигурация раздачи медиафайлов из локальной директории.
 */
@Configuration
public class MediaConfig implements WebMvcConfigurer {

    @Value("${naugram.media.root}")
    private String mediaRoot;

    @Value("${naugram.media.url-prefix}")
    private String mediaUrlPrefix;

    /**
     * Регистрирует URL-префикс для доступа к локальным файлам.
     *
     * <p>Например, файл {@code E:/NauGramMedia/avatars/users/1/avatar.png}
     * будет доступен по URL {@code /media/avatars/users/1/avatar.png}.</p>
     *
     * @param registry реестр обработчиков статических ресурсов
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(mediaUrlPrefix + "/**")
                .addResourceLocations("file:" + mediaRoot + "/");
    }
}