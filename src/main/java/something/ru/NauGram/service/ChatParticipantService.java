package something.ru.NauGram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import something.ru.NauGram.model.ChatParticipant;
import something.ru.NauGram.repository.ChatParticipantRepository;

import java.util.List;

@Service
public class ChatParticipantService {
    private final ChatParticipantRepository chatParticipantRepository;

    @Autowired
    public ChatParticipantService(ChatParticipantRepository chatParticipantRepository) {
        this.chatParticipantRepository = chatParticipantRepository;
    }


    public boolean isParticipant(Long chatId, Long userId) {
        return chatParticipantRepository.findByChatIdAndUserId(chatId, userId).isPresent();
    }

    public List<ChatParticipant> getParticipants(Long chatId) {
        return chatParticipantRepository.findByChatId(chatId);
    }
}
