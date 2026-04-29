package something.ru.NauGram.repository;

import org.springframework.data.repository.CrudRepository;
import something.ru.NauGram.model.ChatLastRead;

public interface ChatLastReadRepository extends CrudRepository<ChatLastRead, Long> {
}
