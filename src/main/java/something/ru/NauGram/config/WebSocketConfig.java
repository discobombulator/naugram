package something.ru.NauGram.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Конфигурация WebSocket для приложения.
 *
 * <p>Включает поддержку брокера сообщений WebSocket с использованием протокола STOMP.
 * Настраивает конечные точки подключения, префиксы назначения и брокер сообщений
 * для обеспечения обмена сообщениями в реальном времени между клиентами и сервером.
 *
 * <p>Основные настройки:
 * <ul>
 *     <li>Брокер сообщений настроен на префиксы "/topic" (публичные каналы) и "/queue" (приватные каналы)</li>
 *     <li>Префикс приложения "/app" используется для маршрутизации сообщений к обработчикам</li>
 *     <li>Префикс пользователя "/user" используется для отправки сообщений конкретным пользователям</li>
 *     <li>Конечная точка WebSocket доступна по пути "/ws" с поддержкой SockJS</li>
 * </ul>
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * Настраивает брокер сообщений WebSocket.
     *
     * <p>Конфигурация включает:
     * <ul>
     *     <li>Включение простого брокера в памяти для каналов с префиксами "/topic" и "/queue"</li>
     *     <li>Установку префикса "/app" для сообщений, направляемых к методам-обработчикам с аннотацией {@code @MessageMapping}</li>
     *     <li>Установку префикса "/user" для отправки сообщений конкретным пользователям через {@code convertAndSendToUser}</li>
     * </ul>
     *
     * @param config реестр конфигурации брокера сообщений, используемый для настройки параметров брокера
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue", "/notify");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Регистрирует конечные точки STOMP для подключения WebSocket-клиентов.
     *
     * <p>Настраивает:
     * <ul>
     *     <li>Конечную точку по пути "/ws" для установки WebSocket-соединений</li>
     *     <li>Разрешение подключений с любого источника (CORS) через шаблон "*"</li>
     *     <li>Поддержку SockJS для клиентов, не поддерживающих нативные WebSocket</li>
     * </ul>
     *
     * @param registry реестр конечных точек STOMP, используемый для регистрации путей подключения
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}