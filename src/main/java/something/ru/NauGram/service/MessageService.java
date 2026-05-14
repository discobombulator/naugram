package something.ru.NauGram.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import something.ru.NauGram.dto.MessageDTO;
import something.ru.NauGram.model.Chat;
import something.ru.NauGram.model.Message;
import something.ru.NauGram.model.User;
import something.ru.NauGram.repository.MessageRepository;

import java.util.List;

@Service
public class MessageService {
    private final int MESSAGE_NUMBER = 5;

    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Message saveMessage(Chat chat,
                               User sender,
                               Message repliedTo,
                               String messageText) {
        Message m = new Message();
        m.setChat(chat);
        m.setSender(sender);
        m.setRepliedTo(repliedTo);
        m.setMessageText(messageText);
        messageRepository.save(m);

        return m;
    }

    public List<MessageDTO> getInitialMessages(Long chatId) {
        return messageRepository.findLastMessagesByChatId(
                chatId,
                PageRequest.of(0, MESSAGE_NUMBER)
        ).stream().map(Message::toMessageDTO).toList().reversed();
    }
}
