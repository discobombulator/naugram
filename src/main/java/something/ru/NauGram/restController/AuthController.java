package something.ru.NauGram.restController;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import something.ru.NauGram.model.User;
import something.ru.NauGram.service.UserService;
import something.ru.NauGram.service.VerificationTokenService;

import java.util.Date;

/**
 * Главный контроллер приложения NauGram.
 * Обрабатывает запросы связанные с регистрацией, авторизацией
 * и подтверждением email через одноразовый код.
 */
@Controller
public class AuthController {

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;

    /**
     * Конструктор контроллера.
     *
     * @param userService сервис для работы с пользователями
     * @param verificationTokenService сервис для работы с токенами подтверждения
     */
    public AuthController(UserService userService, VerificationTokenService verificationTokenService) {
        this.userService = userService;
        this.verificationTokenService = verificationTokenService;
    }

    /**
     * Отображает страницу регистрации.
     *
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона страницы регистрации
     */
    @GetMapping("/registration")
    public String showRegistrationPage(Model model) {
        return "registration";
    }

    /**
     * Обрабатывает форму регистрации нового пользователя.
     * При успехе генерирует токен подтверждения и отправляет его на email.
     * При ошибке возвращает страницу регистрации с сообщением об ошибке.
     *
     * @param user объект пользователя, заполненный из формы
     * @param model модель для передачи данных в шаблон
     * @param session HTTP сессия для хранения email до подтверждения
     * @return редирект на страницу подтверждения email или страницу регистрации при ошибке
     */
    @PostMapping("/registration")
    public String addUser(User user, Model model, HttpSession session) {

        try {
            userService.registerUser(user);
            verificationTokenService.generateNewToken(user);
            session.setAttribute("pendingEmail", user.getEmail());

            return "redirect:/verify-registration";
        } catch (Exception e) {
            model.addAttribute("message", "Пользователь уже существует");
            return "registration";
        }
    }

    /**
     * Отображает страницу авторизации.
     * Если передан параметр error=bad_credentials — добавляет сообщение об ошибке.
     *
     * @param error необязательный параметр ошибки из URL
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона страницы авторизации
     */
    @GetMapping("/login")
    public String showAuthorizationPage(@RequestParam(required = false) String error, Model model) {
        if ("bad_credentials".equals(error)) {
            model.addAttribute("error", "Неверный логин или пароль");
        }
        return "authorization";
    }

    /**
     * Повторно отправляет код подтверждения на email пользователя.
     * Удаляет старый токен и генерирует новый.
     *
     * @param session HTTP сессия с email пользователя
     * @param model модель для передачи данных в шаблон
     * @return HTTP 200 OK при успешной отправке
     * @throws MessagingException если произошла ошибка при отправке email
     */
    @PostMapping("/resend-code")
    @ResponseBody
    public ResponseEntity<?> resendCode(HttpSession session, Model model) throws MessagingException {
        String email = (String) session.getAttribute("pendingEmail");

        User user = userService.findByEmail(email);
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Email not found in session");
        }
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        verificationTokenService.deleteOldToken(user);
        verificationTokenService.generateNewToken(user);

        return ResponseEntity.ok("OK");
    }


    /**
     * Отображает страницу подтверждения email при регистрации.
     *
     * @param session HTTP сессия с email пользователя
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона страницы подтверждения регистрации
     */
    @GetMapping("/verify-registration")
    public String showVerifyRegistrationPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("pendingEmail");

        if (email == null || email.isEmpty()) {
            model.addAttribute("error", "Сессия истекла. Пожалуйста, зарегистрируйтесь заново.");
            return "redirect:/registration";
        }
        model.addAttribute("email", email);
        return "confirmEmailRegistration";
    }

    /**
     * Обрабатывает введённый пользователем код подтверждения при регистрации.
     * Проверяет код и срок его действия. При успехе активирует аккаунт пользователя.
     *
     * @param otpCode код подтверждения введённый пользователем
     * @param session HTTP сессия с email пользователя
     * @param model модель для передачи данных в шаблон
     * @return редирект на страницу входа при успехе, или страницу подтверждения при ошибке
     */
    @PostMapping("/verify-registration")
    public String verifyRegistration(@RequestParam("otp_code") String otpCode,
                                     HttpSession session, Model model){

        String email = (String) session.getAttribute("pendingEmail");
        User user = userService.findByEmail(email);

        if (user == null) {
            model.addAttribute("error", "Сессия истекла. Пожалуйста, авторизуйтесь заново.");
            return "redirect:/registration";
        }
        if (email == null || email.isEmpty()) {
            model.addAttribute("error", "Сессия истекла. Пожалуйста, зарегистрируйтесь заново.");
            return "redirect:/registration";
        }

        String trustToken = verificationTokenService.getToken(user);
        Date trustDate = verificationTokenService.getDate(user);

        if(otpCode.equals(trustToken) && trustDate.after(new Date())){
            user.setEnabled(true);
            userService.saveEnabled(email);
            session.removeAttribute("pendingEmail");
            model.addAttribute("success", "Почта подтверржденна");
            verificationTokenService.deleteOldToken(user);
            return "redirect:/login";
        }
        else if (!otpCode.equals(trustToken)) {
            model.addAttribute("error", "Неверный код");
            return "confirmEmailRegistration";

        }
        else{
            model.addAttribute("error", "Время кода истекло");
            return "confirmEmailRegistration";
        }
    }

    /**
     * Отображает страницу подтверждения email при входе.
     *
     * @param session HTTP сессия с email пользователя
     * @param model модель для передачи данных в шаблон
     * @return имя шаблона страницы подтверждения входа
     */
    @GetMapping("/verify-login")
    public String showVerifyLoginPage(HttpSession session, Model model) {
        String email = (String) session.getAttribute("pendingName");
        User user = userService.findByEmail(email);

        if (user == null || email == null || email.isEmpty()) {
            model.addAttribute("error", "Сессия истекла. Пожалуйста, авторизуйтесь заново.");
            return "redirect:/login";
        }
        model.addAttribute("email", email);

        return "confirmEmailLogin";
    }

    /**
     * Обрабатывает введённый пользователем код подтверждения при входе.
     * Проверяет код, срок действия и статус активации аккаунта.
     * При успехе авторизует пользователя и перенаправляет на главную страницу.
     *
     * @param otpCode код подтверждения введённый пользователем
     * @param session HTTP сессия с email пользователя
     * @param model модель для передачи данных в шаблон
     * @return редирект на главную страницу при успехе, или страницу подтверждения при ошибке
     */
    @PostMapping("/verify-login")
    public String verifyLogin(@RequestParam("otp_code") String otpCode,
                              HttpSession session, Model model){

        String email = (String) session.getAttribute("pendingName");
        User user = userService.findByEmail(email);

        if (email == null || email.isEmpty()) {
            model.addAttribute("error", "Сессия истекла. Пожалуйста, авторизуйтесь заново.");
            return "redirect:/login";
        }

        String trustToken = verificationTokenService.getToken(user);
        Date trustDate = verificationTokenService.getDate(user);

        boolean isConfirmed = user.isEnabled();
        if(otpCode.equals(trustToken) && trustDate.after(new Date()) && isConfirmed){
            userService.saveEnabled(email);
            session.removeAttribute("pendingEmail");
            session.removeAttribute("pendingName");
            verificationTokenService.deleteOldToken(user);
            return "redirect:/";
        }
        else if (!otpCode.equals(trustToken)) {
            model.addAttribute("error", "Неверный код");
            return "confirmEmailLogin";

        }
        else if(!trustDate.after(new Date())){
            model.addAttribute("error", "Время кода истекло");
            return "confirmEmailLogin";
        }
        else{
            model.addAttribute("error", "Вы не подтвердили почту");
            return "confirmEmailLogin";
        }
    }
}
