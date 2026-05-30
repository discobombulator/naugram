package something.ru.NauGram.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.User;
import something.ru.NauGram.repository.ChatLastReadRepository;

@Slf4j
@Service
@AllArgsConstructor
public class ChatLastReadService {
    private final ChatLastReadRepository chatLastReadRepository;

    public int getUnreadMessages(User user, Chat chat) {
        return 7;
    }

    public String getLastMessage(User currentUser, Chat chat) {
        return "Yagondon";
    }
}
