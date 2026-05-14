package something.ru.NauGram.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import something.ru.NauGram.dto.MessageDTO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сущность сообщения в чате.
 *
 * <p>Представляет собой отдельное сообщение, отправленное пользователем в рамках чата.
 * Сообщение может содержать текст, вложения, а также ссылку на другое сообщение
 * (в случае ответа).</p>
 */
@Entity
@Data
public class Message {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replied_to_message_id")
    private Message repliedTo;

    private String messageText;

    private LocalDateTime createdAt;

    private Boolean isEdited;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public MessageDTO toMessageDTO() {
        MessageDTO m = new MessageDTO();
        m.setText(messageText);
        m.setSender(sender.getUsername());
        m.setChatId(chat.getId());
        m.setTimestamp(String.valueOf(createdAt));
        return m;
    }
}
