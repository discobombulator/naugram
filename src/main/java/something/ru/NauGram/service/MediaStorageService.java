package something.ru.NauGram.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import something.ru.NauGram.model.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

/**
 * Сервис для работы с локальными медиафайлами.
 *
 * <p>Сохраняет пользовательские файлы на диск и возвращает URL,
 * который можно сохранить в базе данных.</p>
 */
@Service
public class MediaStorageService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Value("${naugram.media.root}")
    private String mediaRoot;

    @Value("${naugram.media.url-prefix}")
    private String mediaUrlPrefix;

    /**
     * Сохраняет аватар пользователя на локальный диск.
     *
     * @param user пользователь, которому принадлежит аватар
     * @param file загружаемый файл изображения
     * @return публичный URL сохранённого файла
     */
    public String saveUserAvatar(User user, MultipartFile file) {
        validateImage(file);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = "avatar_" + UUID.randomUUID() + extension;

        Path userAvatarDir = Path.of(
                mediaRoot,
                "avatars",
                "users",
                String.valueOf(user.getId())
        );

        try {
            Files.createDirectories(userAvatarDir);

            Path targetPath = userAvatarDir.resolve(fileName);
            file.transferTo(targetPath.toFile());

            return mediaUrlPrefix + "/avatars/users/" + user.getId() + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить аватар", e);
        }
    }

    /**
     * Проверяет, что файл является допустимым изображением.
     *
     * @param file файл для проверки
     */
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Файл не выбран");
        }

        if (!ALLOWED_IMAGE_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Допустимы только JPG, PNG или WEBP");
        }
    }

    /**
     * Возвращает расширение файла.
     *
     * @param originalFilename исходное имя файла
     * @return расширение файла с точкой
     */
    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }

        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    private static final Set<String> ALLOWED_CHAT_MEDIA_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "video/mp4",
            "video/webm"
    );

    public String saveChatMedia(Long chatId, MultipartFile file) {
        validateChatMedia(file);

        String extension = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        Path chatMediaDir = Path.of(
                mediaRoot,
                "chats",
                String.valueOf(chatId)
        );

        try {
            Files.createDirectories(chatMediaDir);

            Path targetPath = chatMediaDir.resolve(fileName);
            file.transferTo(targetPath.toFile());

            return mediaUrlPrefix + "/chats/" + chatId + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Не удалось сохранить медиафайл", e);
        }
    }

    private void validateChatMedia(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Файл не выбран");
        }

        if (!ALLOWED_CHAT_MEDIA_TYPES.contains(file.getContentType())) {
            throw new RuntimeException("Допустимы только изображения JPG, PNG, WEBP, GIF и видео MP4, WEBM");
        }
    }
}