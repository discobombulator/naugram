package something.ru.NauGram.config;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import something.ru.NauGram.model.User;
import something.ru.NauGram.service.UserService;
import something.ru.NauGram.service.VerificationTokenService;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig{

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;

    public SpringSecurityConfig(UserService userService,
                                VerificationTokenService verificationTokenService) {
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
    }

    /**
     * Настраивает цепочку фильтров безопасности
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/login", "/registration","/verify-login", "/resend-code",
                                "/verify-registration","/logo.png", "/static/**", "/ws/**")
                        .permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).hasRole("ADMIN")

                        .requestMatchers("/chats/**", "/api/**").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/chats", true)
                        .permitAll()
                )
//                .formLogin(form -> form
//                        .loginPage("/login") // Ссылка, по которой открывается страница входа
//                        .loginProcessingUrl("/login") // URL, на который форма отправляет POST
//                        .successHandler((request, response, authentication) -> {
//                            try {
//                                HttpSession session = request.getSession();
//                                session.setAttribute("pendingName", authentication.getName());
//                                User user = userService.findByUsername(authentication.getName());
//                                verificationTokenService.deleteOldToken(user);
//                                verificationTokenService.generateNewToken(user);
//                                response.sendRedirect("/verify-login");
//                            } catch (MessagingException e) {
//                                response.sendRedirect("/login?error");
//                            }
//                        })
//                        .failureHandler((request, response, exception) -> {
//                            // Неверный логин/пароль
//                            response.sendRedirect("/login?error=bad_credentials");
//                        })
//                        .permitAll()
//                )
                .rememberMe(remember -> remember
                        .key("naugramSecretKey")
                        .tokenValiditySeconds(2592000)
                        .rememberMeParameter("remember-me")
                        .userDetailsService(userService)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login")
                        .permitAll()
                );

        return http.build();
    }


}