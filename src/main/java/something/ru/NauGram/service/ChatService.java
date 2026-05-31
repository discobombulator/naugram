package something.ru.NauGram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import something.ru.NauGram.dto.CreateChatDTO;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.ChatParticipant;
import something.ru.NauGram.model.ParticipantRole;
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
     * или пустой список, если пользователь не участвует ни в одном чате
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
     * или пустой список, если чат не имеет участников
     */
    public List<User> getChatParticipants(long chatId) {
        return chatParticipantRepository.findByChatId(chatId).stream()
                .map(ChatParticipant::getUser).toList();
    }

    /**
     * Создаёт новый групповой чат и добавляет в него участников.
     *
     * <p>Создаёт чат на основе переданных данных, добавляет в него
     * пользователей из списка {@code userIds}, а также назначает
     * пользователя, создавшего чат, владельцем с ролью
     * {@code OWNER}.</p>
     *
     * <p>Если пользователь из списка участников не найден
     * в базе данных, он пропускается.</p>
     *
     * @param request DTO с данными создаваемого чата:
     *                названием, описанием и списком участников
     * @param userCreatedChat пользователь, создавший чат
     * @return идентификатор созданного чата
     */
    @Transactional
    public Long createChat(CreateChatDTO request, User userCreatedChat) {
        Chat chat = new Chat();
        chat.setChatName(request.getChatName());
        chat.setChatType("group");
        chat.setDescription(request.getDescription());
        chatRepository.save(chat);

        for (var id : request.getUserIds()) {
            Optional<User> optionalU = userRepository.findById(id);
            if (optionalU.isEmpty()) {
                continue;
            }
            User user = optionalU.get();
            ChatParticipant cp = new ChatParticipant(
                    chat,
                    user,
                    ParticipantRole.USER);
            chatParticipantRepository.save(cp);
        }

        ChatParticipant cp = new ChatParticipant(
                chat,
                userCreatedChat,
                ParticipantRole.OWNER);
        chatParticipantRepository.save(cp);

        return chat.getId();
    }

    public User getCompanion(Chat chat, User currentUser) {
        return getChatParticipants(chat.getId())
                .stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .findFirst()
                .orElse(null);
    }
}