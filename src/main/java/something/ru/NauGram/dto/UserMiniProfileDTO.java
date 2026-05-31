package something.ru.NauGram.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO для отображения мини-профиля пользователя в чате.
 *
 * <p>Используется при клике на аватарку пользователя рядом с сообщением.
 * Содержит только те данные, которые нужны для всплывающего окна профиля.</p>
 */
@Getter
@Setter
public class UserMiniProfileDTO {
    private Long id;
    private String username;
    private String realName;
    private String bio;
    private String birthDate;
    private String avatarPath;
}