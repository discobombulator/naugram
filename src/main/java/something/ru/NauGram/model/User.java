package something.ru.NauGram.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Сущность пользователя системы.
 *
 * <p>Представляет зарегистрированного пользователя, включая его основные
 * персональные данные, настройки профиля и роль в системе.</p>
 */
@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String realName;

    private LocalDate birthDate;

    private LocalDateTime createdAt;

    private String profileImagePath;

    private String password;

    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "enabled")
    private boolean enabled;

    public User() {
        super();
        this.enabled = false;
    }

    @Column(name = "enabled")
    private boolean enabled;

    public User() {
        super();
        this.enabled=false;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}