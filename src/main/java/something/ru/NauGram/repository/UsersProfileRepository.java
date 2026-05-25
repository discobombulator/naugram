package something.ru.NauGram.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.UsersProfile;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Репозиторий для работы с профилями пользователей.
 *
 * <p>Предоставляет методы поиска профиля по пользователю или идентификатору,
 * а также методы частичного обновления отдельных полей профиля.</p>
 */
public interface UsersProfileRepository extends CrudRepository<UsersProfile, Long> {

    /**
     * Находит профиль, связанный с указанным пользователем.
     *
     * @param user пользователь, чей профиль необходимо найти
     * @return {@link Optional} с профилем пользователя, если он существует
     */
    Optional<UsersProfile> findByUser(User user);

}