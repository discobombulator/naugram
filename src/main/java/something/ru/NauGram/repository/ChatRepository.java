package something.ru.NauGram.repository;

import org.springframework.data.repository.CrudRepository;
import something.ru.NauGram.model.Chat;

public interface ChatRepository extends CrudRepository<Chat, Long> {
}
