package something.ru.NauGram.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import something.ru.NauGram.dto.MessageDTO;
import something.ru.NauGram.dto.MessageMediaDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType messageType = MessageType.TEXT;

    private String mediaUrl;

    private String mediaContentType;

    private String mediaOriginalName;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageMedia> mediaFiles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public MessageDTO toMessageDTO() {
        MessageDTO m = new MessageDTO();
        m.setId(id);
        m.setText(messageText);
        m.setSender(sender.getUsername());
        m.setSenderId(sender.getId());
        m.setChatId(chat.getId());
        m.setTimestamp(String.valueOf(createdAt));
        m.setMessageType(messageType.name());

        if (mediaFiles != null) {
            m.setMediaFiles(
                    mediaFiles.stream()
                            .sorted(Comparator.comparing(
                                    MessageMedia::getMediaOrder,
                                    Comparator.nullsLast(Integer::compareTo)
                            ))
                            .map(media -> {
                                MessageMediaDTO dto = new MessageMediaDTO();
                                dto.setId(media.getId());
                                dto.setMediaType(media.getMediaType());
                                dto.setMediaPath(media.getMediaPath());
                                dto.setMediaOrder(media.getMediaOrder());
                                return dto;
                            })
                            .toList()
            );
        }

        return m;
    }
}
