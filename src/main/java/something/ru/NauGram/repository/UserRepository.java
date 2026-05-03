package something.ru.NauGram.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import something.ru.NauGram.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    /** Находит пользователей по имени */
    User findByUsername(String name);

    /** Находит пользователя по email (для авторизации) */
    User findByEmail(String email);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.enabled = true WHERE u.email = :email")
    void enableUser(@Param("email") String email);
}
