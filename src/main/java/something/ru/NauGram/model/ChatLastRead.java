package something.ru.NauGram.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность, хранящая информацию о том,
 * какой пользователь в каком чате прочитал
 * последнее сообщение и когда это произошло.
 */
@Entity
@Data
public class ChatLastRead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "chat_participant")
    private ChatParticipant chatParticipant;

    private Long lastReadMessageId;

    private LocalDateTime lastReadAt;

    @PrePersist
    protected void onCreate() {
        lastReadAt = LocalDateTime.now();
    }
}