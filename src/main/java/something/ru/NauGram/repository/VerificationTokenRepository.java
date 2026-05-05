package something.ru.NauGram.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.VerificationToken;

import java.util.Date;

/**
 * Репозиторий для работы с токенами подтверждения email.
 * Предоставляет методы для поиска, удаления и очистки токенов в БД.
 */
@Repository
public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long> {

    /**
     * Находит токен подтверждения по email пользователя.
     *
     * @param email email пользователя
     * @return токен подтверждения или null если не найден
     */
    VerificationToken findByUserEmail(String email);

    /**
     * Находит токен подтверждения по объекту пользователя.
     *
     * @param user объект пользователя
     * @return токен подтверждения или null если не найден
     */
    VerificationToken findByUser(User user);

    /**
     * Удаляет токен подтверждения привязанный к указанному пользователю.
     *
     * @param user пользователь чей токен нужно удалить
     */
    @Transactional
    void deleteVerificationTokenByUser(User user);

    /**
     * Удаляет все просроченные токены у которых дата истечения раньше указанной.
     * Используется планировщиком для периодической очистки БД.
     *
     * @param date дата относительно которой проверяется срок действия токенов
     */
    @Modifying
    @Transactional
    void deleteAllByExpiryDateBefore(Date date);


}