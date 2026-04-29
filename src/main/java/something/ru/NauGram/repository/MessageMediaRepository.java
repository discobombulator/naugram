package something.ru.NauGram.repository;

import org.springframework.data.repository.CrudRepository;
import something.ru.NauGram.model.MessageMedia;

public interface MessageMediaRepository extends CrudRepository<MessageMedia, Long> {
}
