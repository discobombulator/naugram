package something.ru.NauGram.repository;

import org.springframework.data.repository.CrudRepository;
import something.ru.NauGram.model.ChatParticipant;

public interface ChatParticipantRepository extends CrudRepository<ChatParticipant, Long> {
}
