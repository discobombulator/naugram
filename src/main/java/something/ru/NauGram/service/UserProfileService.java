package something.ru.NauGram.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.UsersProfile;
import something.ru.NauGram.repository.UserRepository;
import something.ru.NauGram.repository.UsersProfileRepository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Сервис для работы с профилями пользователей.
 *
 * <p>Отвечает за создание профиля при регистрации пользователя, обновление
 * пользовательских данных профиля, проверку уникальности тега пользователя
 * и поиск профиля по пользователю.</p>
 */
@Service
public class UserProfileService {

    private final UsersProfileRepository usersProfileRepository;
    private final UserRepository userRepository;

    /**
     * Создаёт сервис профилей пользователей.
     *
     * @param usersProfileRepository репозиторий для работы с профилями пользователей
     * @param userRepository репозиторий для работы с пользователями
     */
    public UserProfileService(UsersProfileRepository usersProfileRepository,
                              UserRepository userRepository) {
        this.usersProfileRepository = usersProfileRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создаёт профиль для нового пользователя.
     *
     * <p>Метод вызывается после сохранения пользователя в базе данных.
     * По умолчанию в качестве отображаемого имени используется username
     * пользователя.</p>
     *
     * @param user пользователь, для которого необходимо создать профиль
     */
    public void createUsersProfile(User user) {
        UsersProfile usersProfile = new UsersProfile();

        usersProfile.setUser(user);
        usersProfile.setRealName(user.getUsername());

        usersProfileRepository.save(usersProfile);
    }

    /**
     * Обновляет данные профиля текущего пользователя.
     *
     * <p>Метод обновляет тег пользователя, отображаемое имя, описание профиля
     * и дату рождения. Перед изменением тега выполняется нормализация и проверка
     * уникальности, чтобы два пользователя не могли использовать одинаковый тег.</p>
     *
     * @param currentUser текущий авторизованный пользователь
     * @param firstName новое имя пользователя
     * @param lastName новая фамилия пользователя
     * @param username новый тег пользователя, может быть передан с символом {@code @}
     * @param bio новое описание профиля
     * @param birthDate дата рождения в формате {@code yyyy-MM-dd}, может отсутствовать
     * @throws RuntimeException если тег пустой, имеет неверный формат, уже занят другим
     *                          пользователем или профиль текущего пользователя не найден
     */
    @Transactional
    public void updateProfile(User currentUser,
                              String firstName,
                              String lastName,
                              String username,
                              String bio,
                              String birthDate) {

        username = normalizeUsername(username);

        User userWithSameUsername = userRepository.findByUsername(username);

        if (userWithSameUsername != null &&
                !userWithSameUsername.getId().equals(currentUser.getId())) {
            throw new RuntimeException("Такой тег уже занят");
        }

        currentUser.setUsername(username);
        userRepository.save(currentUser);

        UsersProfile profile = usersProfileRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Профиль не найден"));

        String realName = ((firstName == null ? "" : firstName.trim()) + " " +
                (lastName == null ? "" : lastName.trim())).trim();

        profile.setRealName(realName);
        profile.setDescription(bio);

        if (birthDate != null && !birthDate.isBlank()) {
            profile.setBirthDate(LocalDate.parse(birthDate));
        } else {
            profile.setBirthDate(null);
        }

        usersProfileRepository.save(profile);
    }

    @Transactional
    public void updateAvatar(User user, String avatarPath) {
        UsersProfile profile = usersProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    UsersProfile newProfile = new UsersProfile();
                    newProfile.setUser(user);
                    newProfile.setRealName(user.getUsername());
                    return usersProfileRepository.save(newProfile);
                });

        profile.setProfileImagePath(avatarPath);
        usersProfileRepository.save(profile);
    }
    /**
     * Нормализует и проверяет тег пользователя.
     *
     * <p>Если тег начинается с символа {@code @}, он удаляется перед сохранением.
     * В базе данных тег хранится без символа {@code @}. Допустимый формат:
     * от 3 до 20 символов, латинские буквы, цифры и нижнее подчёркивание.</p>
     *
     * @param username тег пользователя из формы редактирования профиля
     * @return нормализованный тег без символа {@code @}
     * @throws RuntimeException если тег пустой или не соответствует допустимому формату
     */
    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Тег не может быть пустым");
        }

        username = username.trim();

        if (username.startsWith("@")) {
            username = username.substring(1);
        }

        if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
            throw new RuntimeException("Тег должен быть 3-20 символов: буквы, цифры или _");
        }

        return username;
    }

    /**
     * Находит профиль, связанный с указанным пользователем.
     *
     * @param user пользователь, чей профиль необходимо найти
     * @return {@link Optional} с профилем пользователя, если он существует
     */
    public Optional<UsersProfile> findByUser(User user) {
        return usersProfileRepository.findByUser(user);
    }
}