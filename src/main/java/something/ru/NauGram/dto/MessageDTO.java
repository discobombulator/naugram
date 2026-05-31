package something.ru.NauGram.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO для передачи данных сообщения между сервером и клиентом.
 *
 * <p>Используется для отправки информации о сообщении на фронтенд-часть приложения.
 * Содержит основные данные сообщения, необходимые для отображения в пользовательском интерфейсе:
 * текст сообщения, отправителя, идентификатор чата и временную метку.
 */
@Getter
@Setter
public class MessageDTO {
    private String text;
    private String sender;
    private Long senderId;
    private long chatId;
    private String timestamp;

    private String messageType;
    private String mediaUrl;
    private String mediaContentType;
    private String mediaOriginalName;

    private List<MessageMediaDTO> mediaFiles;
    private String senderAvatar;
    private String senderRealName;
}
