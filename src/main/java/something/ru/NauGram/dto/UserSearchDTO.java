package something.ru.NauGram.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для отображения пользователя в результатах поиска.
 *
 * <p>Содержит краткую информацию о пользователе:
 * идентификатор и имя пользователя.</p>
 */
@Data
@AllArgsConstructor
public class UserSearchDTO {
    private Long id;
    private String username;
}