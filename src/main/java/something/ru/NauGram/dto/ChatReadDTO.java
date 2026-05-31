package something.ru.NauGram.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatReadDTO {
    private Long chatId;
    private Long messageId;
}
