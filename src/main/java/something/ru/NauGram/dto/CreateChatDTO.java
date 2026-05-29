package something.ru.NauGram.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO для создания нового чата.
 *
 * <p>Содержит данные, необходимые для создания чата:
 * название, описание и список идентификаторов пользователей,
 * которые будут добавлены в чат.</p>
 */
@Data
public class CreateChatDTO {
    private String chatName;
    private String description;
    private List<Long> userIds;
}
