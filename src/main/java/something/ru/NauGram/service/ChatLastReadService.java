package something.ru.NauGram.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import something.ru.NauGram.model.ChatLastRead;
import something.ru.NauGram.model.ChatParticipant;
import something.ru.NauGram.repository.ChatLastReadRepository;
import something.ru.NauGram.repository.MessageRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ChatLastReadService {
    private final ChatLastReadRepository chatLastReadRepository;
    private final MessageRepository messageRepository;

    public int getUnreadMessages(ChatParticipant cp) throws IllegalArgumentException {
        Optional<ChatLastRead> optionalChatLastRead = chatLastReadRepository.findByChatParticipant(cp);
        log.info("{} have unread {} m's in \"{}\"",
                cp.getUser().getUsername(),
                messageRepository.countByChat(cp.getChat()),
                cp.getChat().getChatName());

        return optionalChatLastRead
                .map(chatLastRead ->
                        messageRepository.countUnreadMessages(
                                cp.getChat(),
                                chatLastRead.getLastReadMessageId()))
                .orElseGet(() ->
                        messageRepository.countByChat(cp.getChat()));

    }

    public void updateLastRead(ChatParticipant cp, Long messageId) {
        Optional<ChatLastRead> optionalChatLastRead =
                chatLastReadRepository.findByChatParticipant(cp);
        ChatLastRead chatLastRead;
        if (optionalChatLastRead.isEmpty()) {
            chatLastRead = new ChatLastRead();
            chatLastRead.setLastReadAt(LocalDateTime.now());
            chatLastRead.setChatParticipant(cp);
            chatLastRead.setLastReadMessageId(messageId);
            chatLastReadRepository.save(chatLastRead);
        } else {
            chatLastRead = optionalChatLastRead.get();
            if (chatLastRead.getLastReadMessageId() > messageId) {
                return;
            }
            chatLastRead.setLastReadAt(LocalDateTime.now());
            chatLastRead.setLastReadMessageId(messageId);
            chatLastReadRepository.save(chatLastRead);
        }
    }

    public Long getLastMessageId(ChatParticipant cp) throws Exception {
        Optional<ChatLastRead> optionalChatLastRead =
                chatLastReadRepository.findByChatParticipant(cp);
        if (optionalChatLastRead.isEmpty()) {
            throw new Exception();
        }
        return optionalChatLastRead.get().getLastReadMessageId();
    }
}
