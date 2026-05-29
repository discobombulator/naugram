package something.ru.NauGram.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import something.ru.NauGram.model.User;

import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {

    /** Находит пользователей по имени */
    User findByUsername(String name);

    /** Находит пользователя по email (для авторизации) */
    User findByEmail(String email);

    /**
     * Активирует аккаунт пользователя по email.
     *
     * <p>Устанавливает значение поля {@code enabled} в {@code true}
     * для пользователя с указанным email.</p>
     *
     * @param email email пользователя, аккаунт которого необходимо активировать
     */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.enabled = true WHERE u.email = :email")
    void enableUser(@Param("email") String email);

    /**
     * Выполняет поиск пользователей по имени пользователя.
     *
     * <p>Возвращает не более 10 пользователей, у которых значение
     * {@code username} содержит указанную строку без учёта регистра.</p>
     *
     * @param username строка для поиска по имени пользователя
     * @return список найденных пользователей, максимум 10 записей
     */
    List<User> findTop10ByUsernameContainingIgnoreCase(String username);
}
