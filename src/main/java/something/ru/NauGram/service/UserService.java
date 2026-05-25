package something.ru.NauGram.service;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.UserRole;
import something.ru.NauGram.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Сервис для работы с пользователями.
 * Реализует {@link UserDetailsService} для интеграции со Spring Security.
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final UserProfileService userProfileService;

    private final PasswordEncoder passwordEncoder;

    /**
     * Конструктор сервиса.
     *
     * @param userRepository репозиторий для работы с пользователями в БД
     * @param passwordEncoder энкодер для хэширования паролей
     */
    public UserService(UserRepository userRepository,
                       UserProfileService userProfileService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProfileService = userProfileService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Регистрирует нового пользователя.
     * Хэширует пароль, назначает роль USER и сохраняет в БД.
     *
     * @param user объект пользователя с данными из формы регистрации
     * @throws RuntimeException если пользователь с таким email уже существует
     */
    @Transactional
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

        userProfileService.createUsersProfile(user);
    }

    /**
     * Изменяет пароль пользователя.
     *
     * <p>Если новый пароль не указан, метод ничего не изменяет. Если новый пароль
     * указан, метод проверяет наличие текущего пароля, сверяет его с сохранённым
     * хэшем и проверяет минимальную длину нового пароля.</p>
     *
     * @param user пользователь, которому необходимо изменить пароль
     * @param oldPassword текущий пароль пользователя
     * @param newPassword новый пароль пользователя
     * @throws RuntimeException если текущий пароль не указан, указан неверно
     *                          или новый пароль короче 6 символов
     */
    @Transactional
    public void changeUserPassword(User user, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return;
        }

        if (oldPassword == null || oldPassword.isBlank()) {
            throw new RuntimeException("Введите текущий пароль");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Текущий пароль указан неверно");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("Новый пароль должен быть минимум 6 символов");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Изменяет выбранный язык интерфейса пользователя.
     *
     * <p>Значение сохраняется в поле {@code language} сущности пользователя
     * и используется для восстановления локализации после перезапуска приложения
     * или новой сессии.</p>
     *
     * @param user пользователь, для которого изменяется язык интерфейса
     * @param lang код языка, например {@code ru} или {@code en}
     */
    @Transactional
    public void changeLocalization(User user, String lang) {
        user.setLanguage(lang);
        userRepository.save(user);
    }

    /**
     * Включает или выключает двухфакторную аутентификацию пользователя.
     *
     * @param user пользователь, для которого изменяется статус 2FA
     * @param faStatus {@code true}, если двухфакторную аутентификацию нужно включить,
     *                 {@code false}, если нужно выключить
     */
    @Transactional
    public void change2FaStatus(User user, boolean faStatus) {
        user.setFaStatus(faStatus);
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
     * @param username username пользователя
     * @return объект {@link UserDetails} с данными пользователя
     * @throws UsernameNotFoundException если пользователь с таким email не найден
     */
    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
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

    /**
     * Находит пользователя по username.
     *
     * @param username username пользователя
     * @return объект {@link User}, если пользователь найден, иначе {@code null}
     */
    public User findByUsername(String username){
        return userRepository.findByUsername(username);
    }
}
