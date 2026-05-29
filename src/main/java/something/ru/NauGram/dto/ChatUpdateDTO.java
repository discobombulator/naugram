package something.ru.NauGram.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatUpdateDTO {
    private Long chatId;
    private String lastMessage;
    private int unreadCount;
}
