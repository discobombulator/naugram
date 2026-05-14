package something.ru.NauGram.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDTO {
    private String text;
    private String sender;
    private long chatId;
    private String timestamp;
}
