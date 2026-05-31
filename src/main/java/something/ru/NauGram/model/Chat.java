package something.ru.NauGram.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>Представляет сущность чата.
 * Чат может быть групповым (chatType=group)
 * и личным (chatType=direct).<p/>
 * <p>id, chatType - обязательные поля<p/>
 * <p>chatName, description, imagePath - могут быть null
 * для direct чатов <p/>
 */
@Entity
@Data
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String chatType; // direct or group
    private String chatName;
    private String description;
    private String imagePath;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY)
    private List<ChatParticipant> participants;

    @OneToMany(mappedBy = "chat", fetch = FetchType.LAZY)
    private List<Message> messages;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
