package something.ru.NauGram;


import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.VerificationToken;

import java.util.Properties;

/**
 * Компонент для отправки email сообщений пользователям.
 * Использует SMTP протокол через Gmail для доставки писем.
 */
@Component
public class TokenSender {

    @Value("${mail.user}")
    private String username;

    @Value("${mail.password}")
    private String password;

    /**
     * Отправляет письмо с кодом подтверждения на email пользователя.
     * Метод выполняется асинхронно чтобы не блокировать основной поток.
     *
     * @param user пользователь которому отправляется письмо
     * @param verificationToken токен содержащий код подтверждения
     * @throws MessagingException если произошла ошибка при отправке письма
     */
    @Async
    public void sendToken(User user, VerificationToken verificationToken) throws MessagingException {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
        message.setSubject("Код подтверждения регистрации");

        String msg = "Здравствуйте, уважаемый пользователь! <br>" +
                "Ваш код подтверждения: <b>" + verificationToken.getToken() + "</b>";

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        Transport.send(message);

    }

}