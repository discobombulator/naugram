package something.ru.NauGram.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ChatLastRead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chatId")
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    private Long lastReadMessageId;

    private LocalDateTime lastReadAt;

    @PrePersist
    protected void onCreate() {
        lastReadAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getLastReadMessageId() {
        return lastReadMessageId;
    }

    public void setLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }

    public LocalDateTime getLastReadAt() {
        return lastReadAt;
    }

    public void setLastReadAt(LocalDateTime lastReadAt) {
        this.lastReadAt = lastReadAt;
    }
}