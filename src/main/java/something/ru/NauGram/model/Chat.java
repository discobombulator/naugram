package something.ru.NauGram.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>Представляет сущность чата.
 * Чат может быть групповым (chatType=group)
 * и личным (chatType=direct).<p/>
 * <p>id, chatType - обязательные поля<p/>
 * <p>chatName, description, imagePath - могут быть null
 *  для direct чатов <p/>
 */
@Entity
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

    public Long getId() {
        return id;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<ChatParticipant> getParticipants() {
        return participants;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
