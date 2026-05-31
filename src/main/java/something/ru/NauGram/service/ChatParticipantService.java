package something.ru.NauGram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.ChatParticipant;
import something.ru.NauGram.model.User;
import something.ru.NauGram.repository.ChatParticipantRepository;

import java.util.Optional;

@Service
public class ChatParticipantService {
    private final ChatParticipantRepository chatParticipantRepository;

    @Autowired
    public ChatParticipantService(ChatParticipantRepository chatParticipantRepository) {
        this.chatParticipantRepository = chatParticipantRepository;
    }

    /**
     * Проверяет, является ли указанный пользователь участником заданного чата.
     * Выполняет поиск записи участника чата по идентификаторам чата и пользователя.
     * Возвращает {@code true}, если запись найдена, иначе {@code false}.
     *
     * @param chatId идентификатор чата, в котором проверяется участие
     * @param userId идентификатор пользователя, для которого проверяется участие в чате
     * @return {@code true} если пользователь является участником чата, {@code false} в противном случае
     */
    public boolean isParticipant(Long chatId, Long userId) {
        return chatParticipantRepository.findByChatIdAndUserId(chatId, userId).isPresent();
    }

    public ChatParticipant getChatParticipant(Chat chat, User user) throws IllegalArgumentException {
        Optional<ChatParticipant> optionalChatParticipant =
                chatParticipantRepository.findByChatIdAndUserId(chat.getId(), user.getId());
        if (optionalChatParticipant.isEmpty()) {
            throw new IllegalArgumentException("Theres is no chat participant with" + user + " " + chat);
        }
        return optionalChatParticipant.get();
    }
}
