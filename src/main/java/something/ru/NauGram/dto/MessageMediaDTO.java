package something.ru.NauGram.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO для передачи данных о медиафайле сообщения на клиентскую сторону.
 *
 * <p>Используется внутри {@link MessageDTO}, когда одно сообщение содержит
 * один или несколько прикреплённых файлов.</p>
 */
@Getter
@Setter
public class MessageMediaDTO {
    private Long id;
    private String mediaType;
    private String mediaPath;
    private Integer mediaOrder;
}