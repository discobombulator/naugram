package something.ru.NauGram.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageMediaDTO {
    private Long id;
    private String mediaType;
    private String mediaPath;
    private Integer mediaOrder;
}