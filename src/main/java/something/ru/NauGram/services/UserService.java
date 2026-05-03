package something.ru.NauGram.services;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.UserRole;
import something.ru.NauGram.repository.UserRepository;

import java.util.List;

/**
 * Сервис для работы с пользователями.
 * Реализует {@link UserDetailsService} для интеграции со Spring Security.
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Конструктор сервиса.
     *
     * @param userRepository репозиторий для работы с пользователями в БД
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

        user.setUsername(user.getEmail());

        userRepository.save(user);
    }

    /**
     * Активирует аккаунт пользователя по email.
     * Устанавливает флаг enabled = true в БД.
     *
     * @param email email пользователя которого нужно активировать
     */
    public void saveEnabled(String email){
        userRepository.enableUser(email);
    }

    /**
     * Загружает данные пользователя по email для Spring Security.
     *
     * @param email email пользователя
     * @return объект {@link UserDetails} с данными пользователя
     * @throws UsernameNotFoundException если пользователь с таким email не найден
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if(user == null){
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
    public User findByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
