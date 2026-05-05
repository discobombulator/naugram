package something.ru.NauGram.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.ChatParticipant;
import something.ru.NauGram.model.ParticipantRole;
import something.ru.NauGram.model.User;
import something.ru.NauGram.repository.ChatParticipantRepository;
import something.ru.NauGram.repository.ChatRepository;
import something.ru.NauGram.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository,
                       UserRepository userRepository,
                       ChatParticipantRepository chatParticipantRepository) {
        this.chatRepository = chatRepository;
        this.userRepository = userRepository;
        this.chatParticipantRepository = chatParticipantRepository;

//        try{
//            User user = userRepository.findByUsername("afk");
//            List<Chat> chats = chatRepository.findByUser(user);
//            log.info("chat at {}", chats.stream().map(Chat::getChatName).toList());
//        } catch (Exception e) {
//            log.error("cannot locate user ", e);
//        }
//
//
//        Chat chat = new Chat();
//        chat.setChatName("AfkChat");
//        chat.setChatType("direct");
//        chatRepository.save(chat);
//
//
//        ChatParticipant cp = new ChatParticipant();
//        cp.setChat(chat);
//        cp.setUser(user);
//        cp.setJoinedAt(LocalDateTime.now());
//        cp.setRole(ParticipantRole.USER);
//        chatParticipantRepository.save(cp);
    }

    public List<Chat> getCurrentUserChats(User user){
        return chatRepository.findByUser(user);
    }

}
