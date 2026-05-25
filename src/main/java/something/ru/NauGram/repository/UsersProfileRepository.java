package something.ru.NauGram.repository;

import org.springframework.data.repository.CrudRepository;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.UsersProfile;

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