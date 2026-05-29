package something.ru.NauGram.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * <p>Сущность, хранящая связь между пользователем и определённым чатом.</p>
 */
@Entity
@Data
@NoArgsConstructor
public class ChatParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParticipantRole role;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    public ChatParticipant(Chat c, User u, ParticipantRole r){
        this.chat = c;
        this.user = u;
        this.role = r;
    }
}