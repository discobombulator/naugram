package something.ru.NauGram.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * Конфигурация локализации приложения.
 *
 * <p>Отвечает за выбор текущего языка интерфейса, обработку параметра
 * {@code lang} в URL и подключение файлов переводов.</p>
 *
 * <p>Файлы локализации должны находиться в директории {@code src/main/resources}
 * и иметь имена вида:</p>
 *
 * <ul>
 *     <li>{@code messages_ru.properties}</li>
 *     <li>{@code messages_en.properties}</li>
 * </ul>
 */
@Configuration
public class LocalizationConfig implements WebMvcConfigurer {

    /**
     * Создаёт resolver для хранения выбранной локали в HTTP-сессии.
     *
     * <p>По умолчанию используется русский язык. Если пользователь выбирает
     * другой язык, локаль сохраняется в текущей сессии.</p>
     *
     * @return resolver локали на основе HTTP-сессии
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("ru"));
        return resolver;
    }

    /**
     * Создаёт interceptor для изменения текущей локали через параметр запроса.
     *
     * <p>Например, запрос {@code /?lang=en} переключит интерфейс на английский,
     * а {@code /?lang=ru} — на русский.</p>
     *
     * @return interceptor изменения локали
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    /**
     * Регистрирует interceptor изменения локали в Spring MVC.
     *
     * <p>После регистрации приложение начинает обрабатывать параметр
     * {@code lang} во входящих HTTP-запросах.</p>
     *
     * @param registry реестр interceptor'ов Spring MVC
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * Создаёт источник сообщений для Thymeleaf и Spring.
     *
     * <p>Использует файлы переводов с базовым именем {@code messages}.
     * Например:</p>
     *
     * <ul>
     *     <li>{@code messages_ru.properties}</li>
     *     <li>{@code messages_en.properties}</li>
     * </ul>
     *
     * <p>Кодировка устанавливается в {@code UTF-8}, чтобы русские символы
     * корректно отображались в интерфейсе.</p>
     *
     * @return источник локализованных сообщений
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("messages");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }
}