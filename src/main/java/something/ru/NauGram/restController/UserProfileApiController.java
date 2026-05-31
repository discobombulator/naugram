package something.ru.NauGram.restController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import something.ru.NauGram.dto.UserMiniProfileDTO;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.UsersProfile;
import something.ru.NauGram.service.UserProfileService;
import something.ru.NauGram.service.UserService;

/**
 * REST-контроллер для получения данных профиля пользователя.
 *
 * <p>Используется клиентской частью чата для отображения мини-профиля
 * пользователя при клике на аватарку рядом с сообщением.</p>
 */
@RestController
@RequestMapping("/api/users")
public class UserProfileApiController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    public UserProfileApiController(UserService userService,
                                    UserProfileService userProfileService) {
        this.userService = userService;
        this.userProfileService = userProfileService;
    }

    /**
     * Возвращает данные мини-профиля пользователя.
     *
     * <p>В ответ включаются тег пользователя, отображаемое имя, описание,
     * дата рождения и путь к аватарке, если профиль существует.</p>
     *
     * @param userId идентификатор пользователя
     * @return DTO мини-профиля пользователя
     */
    @GetMapping("/{userId}/mini-profile")
    public ResponseEntity<UserMiniProfileDTO> getMiniProfile(@PathVariable Long userId) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        UsersProfile profile = userProfileService.findByUser(user).orElse(null);

        UserMiniProfileDTO dto = new UserMiniProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());

        if (profile != null) {
            dto.setRealName(profile.getRealName());
            dto.setBio(profile.getDescription());
            dto.setAvatarPath(profile.getProfileImagePath());

            if (profile.getBirthDate() != null) {
                dto.setBirthDate(profile.getBirthDate().toString());
            }
        }

        return ResponseEntity.ok(dto);
    }
}