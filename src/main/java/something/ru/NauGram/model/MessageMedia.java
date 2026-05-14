package something.ru.NauGram.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Сущность для хранения медиафайлов, связанных с сообщением.
 *
 * <p>Содержит ссылку на сообщение, к которому относится медиафайл,
 * его тип, а также ссылку на ресурс во внешнем хранилище
 * (например, файловая система или объектное хранилище).</p>
 *
 * <p>Дополнительно хранит порядковый номер, определяющий
 * последовательность отображения медиафайлов в рамках сообщения.</p>
 */
@Data
@Entity
public class MessageMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private Message message;

    private String mediaType; // document, photo, video

    private String mediaPath;

    private Integer mediaOrder;
}