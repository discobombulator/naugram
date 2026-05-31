package something.ru.NauGram.repository;

import org.springframework.data.repository.CrudRepository;
import something.ru.NauGram.model.MessageMedia;

import java.util.List;

/**
 * Репозиторий для работы с медиафайлами сообщений.
 *
 * <p>Используется для сохранения, поиска и упорядоченного получения
 * вложений, связанных с конкретным сообщением.</p>
 */
public interface MessageMediaRepository extends CrudRepository<MessageMedia, Long> {

    /**
     * Находит все медиафайлы, прикреплённые к указанному сообщению,
     * отсортированные по порядковому номеру.
     *
     * @param messageId идентификатор сообщения
     * @return список медиафайлов сообщения, отсортированный по {@code mediaOrder}
     */
    List<MessageMedia> findByMessageIdOrderByMediaOrderAsc(Long messageId);
}