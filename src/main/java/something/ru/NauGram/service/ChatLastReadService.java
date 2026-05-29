package something.ru.NauGram.service;

import org.springframework.stereotype.Service;
import something.ru.NauGram.model.User;

@Service
public class ChatLastReadService {

    public int getUnreadMessages(User user) {
        return 7;
    }
}
