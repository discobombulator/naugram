package something.ru.NauGram.repository;

import org.springframework.data.repository.CrudRepository;
import something.ru.NauGram.model.ChatLastRead;
import something.ru.NauGram.model.ChatParticipant;

import java.util.Optional;

public interface ChatLastReadRepository extends CrudRepository<ChatLastRead, Long> {
    Optional<ChatLastRead> findByChatParticipant(ChatParticipant chatParticipant);
}
