package something.ru.NauGram.repository;

import org.springframework.data.repository.CrudRepository;
import something.ru.NauGram.model.ChatParticipant;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends CrudRepository<ChatParticipant, Long> {
    List<ChatParticipant> findByChatId(long chatId);

    Optional<ChatParticipant> findByChatIdAndUserId(long chatId, long userId);
}
