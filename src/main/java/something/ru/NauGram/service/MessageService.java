package something.ru.NauGram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import something.ru.NauGram.dto.MessageDTO;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.Message;
import something.ru.NauGram.model.MessageType;
import something.ru.NauGram.model.User;
import something.ru.NauGram.repository.MessageRepository;

import java.util.List;

/**
 * Сервис для работы с сообщениями чата.
 * Предоставляет методы для сохранения новых сообщений и получения
 * последних сообщений чата с ограничением по количеству.
 */
@Service
public class MessageService {
    /** Количество последних сообщений, возвращаемых при начальной загрузке чата */
    private final int MESSAGE_NUMBER = 50;

    private final MessageRepository messageRepository;

    /**
     * Конструктор сервиса сообщений.
     * Выполняет внедрение зависимости репозитория сообщений через параметр конструктора.
     *
     * @param messageRepository репозиторий для работы с сущностью {@link Message}
     */
    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Сохраняет новое сообщение в базе данных.
     * Создает объект сообщения с указанными параметрами и сохраняет его в репозитории.
     * Метод выполняется в транзакционном контексте.
     *
     * @param chat        чат, в который отправляется сообщение
     * @param sender      пользователь, отправляющий сообщение
     * @param repliedTo   сообщение, на которое отвечает текущее (может быть {@code null})
     * @param messageText текст отправляемого сообщения
     * @return сохраненный объект сообщения с присвоенным идентификатором
     */
    @Transactional
    public Message saveMessage(Chat chat,
                               User sender,
                               Message repliedTo,
                               String messageText) {
        Message m = new Message();
        m.setChat(chat);
        m.setSender(sender);
        m.setRepliedTo(repliedTo);
        m.setMessageText(messageText);
        messageRepository.save(m);

        return m;
    }

    /**
     * Получает начальный набор последних сообщений для указанного чата.
     * Возвращает ограниченное количество сообщений (задается константой {@link #MESSAGE_NUMBER}),
     * отсортированных в хронологическом порядке (от старых к новым).
     * Сообщения преобразуются в DTO для передачи на клиентскую сторону.
     *
     * @param chatId идентификатор чата, для которого запрашиваются сообщения
     * @return список DTO сообщений в хронологическом порядке (от старых к новым),
     *         содержащий не более {@link #MESSAGE_NUMBER} последних сообщений
     */
    public List<MessageDTO> getInitialMessages(Long chatId) {
        return messageRepository.findLastMessagesByChatId(
                chatId,
                PageRequest.of(0, MESSAGE_NUMBER)
        ).stream().map(Message::toMessageDTO).toList().reversed();
    }

    @Transactional
    public Message saveMediaMessage(Chat chat,
                                    User sender,
                                    String mediaUrl,
                                    String contentType,
                                    String originalName) {
        Message message = new Message();

        message.setChat(chat);
        message.setSender(sender);
        message.setMessageText(originalName);

        if (contentType != null && contentType.startsWith("image/")) {
            message.setMessageType(MessageType.IMAGE);
        } else if (contentType != null && contentType.startsWith("video/")) {
            message.setMessageType(MessageType.VIDEO);
        } else {
            throw new RuntimeException("Неподдерживаемый тип медиа");
        }

        message.setMediaUrl(mediaUrl);
        message.setMediaContentType(contentType);
        message.setMediaOriginalName(originalName);

        return messageRepository.save(message);
    }
}