package something.ru.NauGram.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.User;

import java.util.List;

public interface ChatRepository extends CrudRepository<Chat, Long> {
    @Query("SELECT DISTINCT c FROM Chat c " +
            "LEFT JOIN FETCH c.messages m " +
            "JOIN c.participants p " +
            "WHERE p.user = :user ")
    List<Chat> findByUser(@Param("user") User user);
}
