package something.ru.NauGram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.ChatParticipant;
import something.ru.NauGram.model.User;
import something.ru.NauGram.repository.ChatParticipantRepository;
import something.ru.NauGram.repository.ChatRepository;
import something.ru.NauGram.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Сервис для управления чатами.
 * Предоставляет методы для получения информации о чатах,
 * их участниках и выполнения операций, связанных с чатами.
 */
@Slf4j
@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    /**
     * Конструктор сервиса чатов.
     * Выполняет внедрение зависимостей репозиториев через параметры конструктора.
     *
     * @param chatRepository            репозиторий для работы с сущностью {@link Chat}
     * @param userRepository            репозиторий для работы с сущностью {@link User}
     * @param chatParticipantRepository репозиторий для работы с сущностью {@link ChatParticipant}
     */
    @Autowired
    public ChatService(ChatRepository chatRepository,
                       UserRepository userRepository,
                       ChatParticipantRepository chatParticipantRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.chatParticipantRepository = chatParticipantRepository;
    }

    /**
     * Получает список всех чатов, в которых участвует указанный пользователь.
     * Выполняет поиск чатов по объекту пользователя в репозитории.
     *
     * @param user пользователь, для которого необходимо получить список чатов
     * @return список чатов, в которых пользователь является участником,
     *         или пустой список, если пользователь не участвует ни в одном чате
     */
    public List<Chat> getCurrentUserChats(User user) {
        return chatRepository.findByUser(user);
    }

    /**
     * Получает чат по его идентификатору.
     * Если чат с указанным идентификатором не найден, выбрасывается исключение.
     * При отсутствии чата в базе данных выполняется логирование ошибки.
     *
     * @param chatId идентификатор запрашиваемого чата
     * @return объект чата с указанным идентификатором
     * @throws NoSuchElementException если чат с указанным идентификатором не существует
     */
    public Chat getChat(Long chatId) {
        Optional<Chat> chatOptional = chatRepository.findById(chatId);
        if (chatOptional.isEmpty()) {
            log.error("There is no chat with such id {}", chatId);
            throw new NoSuchElementException();
        } else {
            return chatOptional.get();
        }
    }

    /**
     * Получает список всех участников указанного чата.
     * Извлекает записи участников чата по идентификатору чата
     * и преобразует их в список пользователей.
     *
     * @param chatId идентификатор чата, для которого необходимо получить участников
     * @return список пользователей, являющихся участниками указанного чата,
     *         или пустой список, если чат не имеет участников
     */
    public List<User> getChatParticipants(long chatId){
        return chatParticipantRepository.findByChatId(chatId).stream()
                .map(ChatParticipant::getUser).toList();
    }
}