package something.ru.NauGram.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateChatDTO {
    private String chatName;
    private String description;
    private List<Long> userIds;
}
