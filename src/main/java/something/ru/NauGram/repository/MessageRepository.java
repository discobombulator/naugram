package something.ru.NauGram.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.Message;

import java.util.List;

public interface MessageRepository extends CrudRepository<Message, Long> {
    /**
     * Находит последние сообщения для указанного чата с возможностью ограничения количества.
     * Сообщения возвращаются в порядке убывания идентификатора (от новых к старым).
     * Использует объект {@link PageRequest} для указания смещения и лимита результатов.
     *
     * @param chatId   идентификатор чата, для которого необходимо получить сообщения
     * @param pageable объект пагинации, содержащий информацию о смещении (page) и количестве (size) записей
     * @return список сообщений указанного чата, отсортированных по убыванию идентификатора,
     * ограниченный параметрами пагинации
     */
    @Query("""
                SELECT m
                FROM Message m
                WHERE m.chat.id = :chatId
                ORDER BY m.id DESC
            """)
    List<Message> findLastMessagesByChatId(@Param("chatId") Long chatId, PageRequest pageable);

    @Query("""
                select count(m)
                from Message m
                where m.chat = :chat
                  and m.id > :lastReadMessageId
            """)
    int countUnreadMessages(
            @Param("chat") Chat chat,
            @Param("lastReadMessageId")
            Long lastReadMessageId
    );

    int countByChat(Chat chat);

    @Query("""
                SELECT m
                FROM Message m
                WHERE m.chat.id = :chatId
                ORDER BY m.id ASC
            """)
    List<Message> findFirstMessageByChatId(@Param("chatId") Long chatId);
}
