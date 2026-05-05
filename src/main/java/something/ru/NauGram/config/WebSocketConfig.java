package something.ru.NauGram.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import something.ru.NauGram.handler.MessageHandler;
import something.ru.NauGram.service.UserService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final UserService userService;

    public WebSocketConfig(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new MessageHandler(userService), "/chat/{roomId}")
//                .addInterceptors(new AuthHandshakeInterceptor(userService))
                .setAllowedOrigins("*"); // Restrict in production
    }
}