package something.ru.NauGram.service;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.UserRole;
import something.ru.NauGram.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с пользователями.
 * Реализует {@link UserDetailsService} для интеграции со Spring Security.
 */
@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Конструктор сервиса.
     *
     * @param userRepository  репозиторий для работы с пользователями в БД
     * @param passwordEncoder энкодер для хэширования паролей
     */
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Регистрирует нового пользователя.
     * Хэширует пароль, назначает роль USER и сохраняет в БД.
     *
     * @param user объект пользователя с данными из формы регистрации
     * @throws RuntimeException если пользователь с таким email уже существует
     */
    public void registerUser(User user) {

        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Пользователь уже существует");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setRole(UserRole.USER);

        String email = user.getEmail();
        user.setEmail(email);
        user.setUsername(email.substring(0, email.indexOf("@")));

        userRepository.save(user);
    }

    /**
     * Активирует аккаунт пользователя по email.
     * Устанавливает флаг enabled = true в БД.
     *
     * @param email email пользователя которого нужно активировать
     */
    public void saveEnabled(String email) {
        userRepository.enableUser(email);
    }

    /**
     * Загружает данные пользователя по email для Spring Security.
     *
     * @param username username пользователя
     * @return объект {@link UserDetails} с данными пользователя
     * @throws UsernameNotFoundException если пользователь с таким email не найден
     */
    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())));
    }

    /**
     * Находит пользователя по email.
     *
     * @param email email пользователя
     * @return объект {@link User} или null если не найден
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            log.debug("No authenticated user found");
            return null;
        }

        if ("anonymousUser".equals(auth.getPrincipal())) {
            log.debug("Anonymous user, returning null");
            return null;
        }

        log.debug("Getting current user from authentication: {}", auth.getName());

        Object principal = auth.getPrincipal();


        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();

            return findByUsername(username);
        }

        String username = auth.getName();
        User user = findByEmail(username);
        if (user == null) {
            user = findByUsername(username);
        }

        return user;
    }
}
