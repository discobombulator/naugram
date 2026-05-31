package something.ru.NauGram.model;

/**
 * Тип сообщения в чате.
 *
 * <p>Используется клиентом для выбора способа отображения сообщения:
 * обычный текст, изображение, видео или смешанный набор медиафайлов.</p>
 */
public enum MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    MIXED
}