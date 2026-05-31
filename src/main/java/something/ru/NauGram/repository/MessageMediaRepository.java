package something.ru.NauGram.repository;

import org.springframework.data.repository.CrudRepository;
import something.ru.NauGram.model.MessageMedia;

import java.util.List;

public interface MessageMediaRepository extends CrudRepository<MessageMedia, Long> {

    List<MessageMedia> findByMessageIdOrderByMediaOrderAsc(Long messageId);
}