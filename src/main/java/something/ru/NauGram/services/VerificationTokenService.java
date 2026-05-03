package something.ru.NauGram.services;


import jakarta.mail.MessagingException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import something.ru.NauGram.model.User;
import something.ru.NauGram.model.VerificationToken;
import something.ru.NauGram.repository.VerificationTokenRepository;
import something.ru.NauGram.TokenSender;

import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Сервис для управления токенами подтверждения email.
 * Отвечает за генерацию, проверку и удаление одноразовых кодов подтверждения.
 */
@Service
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final TokenSender tokenSender;

    /**
     * Конструктор сервиса.
     *
     * @param verificationTokenRepository репозиторий для работы с токенами в БД
     * @param tokenSender компонент для отправки кода на email
     */
    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository, TokenSender tokenSender) {
        this.verificationTokenRepository = verificationTokenRepository;
        this.tokenSender = tokenSender;
    }

    /**
     * Удаляет существующий токен подтверждения пользователя если он есть.
     *
     * @param user пользователь чей токен нужно удалить
     */
    @Transactional
    public void deleteOldToken(User user){
        VerificationToken verificationToken = verificationTokenRepository.findByUserEmail(user.getEmail());

        if (verificationToken != null) {
            verificationTokenRepository.deleteVerificationTokenByUser(user);
        }
    }

    /**
     * Генерирует новый одноразовый код подтверждения для пользователя
     * и отправляет его на email. Код состоит из 6 цифр и действует
     * в течение времени указанного в {@link VerificationToken#EXPIRATION}.
     *
     * @param user пользователь для которого генерируется токен
     * @throws MessagingException если произошла ошибка при отправке email
     */
    @Transactional
    public void generateNewToken(User user) throws MessagingException {
        VerificationToken verificationToken = new VerificationToken();

        String newToken = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999 + 1));
        verificationToken.setToken(newToken);
        verificationToken.setExpiryDate(verificationToken.calculateExpiryDate(VerificationToken.EXPIRATION));
        verificationToken.setUser(user);
        verificationTokenRepository.save(verificationToken);
        tokenSender.sendToken(user,verificationToken);

    }

    /**
     * Возвращает код подтверждения привязанный к пользователю.
     *
     * @param user пользователь чей код нужно получить
     * @return строка с кодом подтверждения
     */
    @Transactional
    public String getToken(User user){
        return verificationTokenRepository.findByUserEmail(user.getEmail()).getToken();
    }

    /**
     * Возвращает дату истечения токена подтверждения пользователя.
     *
     * @param user пользователь чью дату истечения нужно получить
     * @return дата истечения токена
     */
    @Transactional
    public Date getDate(User user){
        return verificationTokenRepository.findByUserEmail(user.getEmail()).getExpiryDate();
    }

    /**
     * Автоматически удаляет все просроченные токены из БД.
     * Запускается каждый час через планировщик Spring.
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void deleteOldTokensAfterTime(){
        verificationTokenRepository.deleteAllByExpiryDateBefore(new Date());
    }


}