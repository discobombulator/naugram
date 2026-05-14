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
import java.util.NoSuchElementException;
import java.util.Optional;

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
//            var cp = chatParticipantRepository.findByChatId(3);
////            List<Chat> chats = chatRepository.findByUser(user);
////            log.info("{} at {}",user.getId(), chat.getId());
//            for (var c : cp){
//                if (c.getUser().getId() == 5){
//                    chatParticipantRepository.delete(c);
//                    break;
//                }
//            }
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
//        cp.setChat(chatRepository.findById(3));
//        cp.setUser(userRepository.findByUsername("afk2"));
//        cp.setJoinedAt(LocalDateTime.now());
//        cp.setRole(ParticipantRole.USER);
//        chatParticipantRepository.save(cp);
    }

    public List<Chat> getCurrentUserChats(User user) {
        return chatRepository.findByUser(user);
    }

    public Chat getChat(Long chatId) {
        Optional<Chat> chatOptional = chatRepository.findById(chatId);
        if (chatOptional.isEmpty()) {
            log.error("There is no chat with such id {}", chatId);
            throw new NoSuchElementException();
        } else {
            return chatOptional.get();
        }
    }

    public List<User> getChatParticipants(long chatId){
        return chatParticipantRepository.findByChatId(chatId).stream()
                .map(ChatParticipant::getUser).toList();
    }
}
