package something.ru.NauGram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import something.ru.NauGram.dto.MessageDTO;
import something.ru.NauGram.model.*;
import something.ru.NauGram.repository.MessageMediaRepository;
import something.ru.NauGram.repository.MessageRepository;
import something.ru.NauGram.repository.UsersProfileRepository;

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

    private final MessageMediaRepository messageMediaRepository;
    private final UsersProfileRepository usersProfileRepository;
    private final MessageRepository messageRepository;

    /**
     * Конструктор сервиса сообщений.
     * Выполняет внедрение зависимости репозитория сообщений через параметр конструктора.
     *
     * @param messageRepository репозиторий для работы с сущностью {@link Message}
     */
    @Autowired
    public MessageService(MessageMediaRepository messageMediaRepository, UsersProfileRepository usersProfileRepository, MessageRepository messageRepository) {
        this.messageMediaRepository = messageMediaRepository;
        this.usersProfileRepository = usersProfileRepository;
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
        ).stream().map(this::toDto).toList().reversed();
    }

    /**
     * Сохраняет сообщение с одним медиафайлом.
     *
     * <p>Метод создаёт сообщение, прикрепляет к нему один медиафайл
     * и определяет тип сообщения на основе MIME-типа файла:
     * {@link MessageType#IMAGE} для изображений и {@link MessageType#VIDEO}
     * для видео.</p>
     *
     * <p>Путь к файлу, MIME-тип и исходное имя файла сохраняются в полях сообщения.
     * Метод оставлен для поддержки старого формата медиа-сообщений, где одно
     * сообщение могло содержать только один файл.</p>
     *
     * @param chat чат, в который отправляется медиа-сообщение
     * @param sender пользователь, отправляющий сообщение
     * @param mediaUrl публичный путь к сохранённому медиафайлу
     * @param contentType MIME-тип файла, например {@code image/png} или {@code video/mp4}
     * @param originalName исходное имя загруженного файла
     * @return сохранённое сообщение с прикреплённым медиафайлом
     * @throws RuntimeException если MIME-тип файла не является изображением или видео
     */
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

    /**
     * Сохраняет сообщение с одним или несколькими медиафайлами.
     *
     * <p>Метод создаёт основную запись сообщения, определяет его тип
     * на основе прикреплённых файлов, сохраняет связанные {@link MessageMedia}
     * и возвращает итоговое сообщение.</p>
     *
     * @param chat чат, в который отправляется сообщение
     * @param sender пользователь, отправляющий сообщение
     * @param text текстовая подпись к медиафайлам, может быть пустой
     * @param mediaPaths публичные пути к сохранённым файлам
     * @param files исходные загруженные файлы
     * @return сохранённое сообщение со связанными медиафайлами
     * @throws RuntimeException если сообщение не содержит ни текста, ни файлов
     */
    @Transactional
    public Message saveMediaMessage(Chat chat,
                                    User sender,
                                    String text,
                                    List<String> mediaPaths,
                                    List<MultipartFile> files) {
        if ((text == null || text.isBlank()) && (files == null || files.isEmpty())) {
            throw new RuntimeException("Сообщение не может быть пустым");
        }

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setMessageText(text != null ? text.trim() : "");

        boolean hasPhoto = files.stream()
                .anyMatch(file -> file.getContentType() != null && file.getContentType().startsWith("image/"));

        boolean hasVideo = files.stream()
                .anyMatch(file -> file.getContentType() != null && file.getContentType().startsWith("video/"));

        if (hasPhoto && hasVideo) {
            message.setMessageType(MessageType.MIXED);
        } else if (hasPhoto) {
            message.setMessageType(MessageType.IMAGE);
        } else if (hasVideo) {
            message.setMessageType(MessageType.VIDEO);
        } else {
            message.setMessageType(MessageType.TEXT);
        }

        Message savedMessage = messageRepository.save(message);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String mediaPath = mediaPaths.get(i);

            MessageMedia media = new MessageMedia();
            media.setMessage(savedMessage);
            media.setMediaPath(mediaPath);
            media.setMediaOrder(i);

            String contentType = file.getContentType();

            if (contentType != null && contentType.startsWith("image/")) {
                media.setMediaType("photo");
            } else if (contentType != null && contentType.startsWith("video/")) {
                media.setMediaType("video");
            } else {
                media.setMediaType("document");
            }

            messageMediaRepository.save(media);
            savedMessage.getMediaFiles().add(media);
        }

        return savedMessage;
    }

    /**
     * Преобразует сущность сообщения в DTO для отправки на клиентскую сторону.
     *
     * <p>Метод использует базовое преобразование {@link Message#toMessageDTO()},
     * после чего дополнительно заполняет данные профиля отправителя:
     * путь к аватарке и отображаемое имя.</p>
     *
     * <p>Эти данные используются на странице чата для отображения аватарки
     * пользователя рядом с сообщением и для корректного отображения мини-профиля.</p>
     *
     * @param message сообщение, которое необходимо преобразовать
     * @return DTO сообщения с данными отправителя и его профиля
     */
    private MessageDTO toDto(Message message) {
        MessageDTO dto = message.toMessageDTO();

        usersProfileRepository.findByUser(message.getSender()).ifPresent(profile -> {
            dto.setSenderAvatar(profile.getProfileImagePath());
            dto.setSenderRealName(profile.getRealName());
        });

        return dto;
    }

    /**
     * Преобразует сущность сообщения в DTO для отправки на клиент.
     *
     * <p>Дополнительно заполняет данные отправителя, такие как аватарка
     * и отображаемое имя из профиля пользователя.</p>
     *
     * @param message сообщение для преобразования
     * @return DTO сообщения
     */
    public MessageDTO convertToDto(Message message) {
        return toDto(message);
    }
}