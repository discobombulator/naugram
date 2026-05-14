package something.ru.NauGram.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import something.ru.NauGram.model.Message;

import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId ORDER BY m.id DESC")
    List<Message> findLastMessagesByChatId(@Param("chatId") Long chatId, PageRequest pageable);
}
