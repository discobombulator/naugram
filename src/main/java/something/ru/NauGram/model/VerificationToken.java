package something.ru.NauGram.model;

import jakarta.persistence.*;
import something.ru.NauGram.model.User;

import java.security.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Entity
public class VerificationToken {
    public static final int EXPIRATION = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    private Date expiryDate;

    public String getToken() {
        return token;
    }
    public Date getExpiryDate() {
        return expiryDate;
    }
    public User getUser() {
        return user;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }

}
