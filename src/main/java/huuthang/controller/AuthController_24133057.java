package huuthang.controller;

import huuthang.config.ServiceFactory_24133057;
import huuthang.model.User_24133057;
import huuthang.service.EmailService_24133057;
import huuthang.service.UserService_24133057;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthController_24133057 extends BaseController_24133057 {
    private static final long serialVersionUID = 1L;
    private static final long OTP_VALID_SECONDS = 300;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private transient UserService_24133057 userService;
    private transient EmailService_24133057 emailService;
    private transient SecureRandom secureRandom;

    @Override
    public void init() {
        userService = ServiceFactory_24133057.userService();
        emailService = ServiceFactory_24133057.emailService();
        secureRandom = new SecureRandom();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "/auth/logout": logout(request, response); break;
            case "/auth/register": render(request, response, "register"); break;
            case "/auth/verify-otp": showOtp(request, response); break;
            default: showLogin(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "/auth/register": register(request, response); break;
            case "/auth/verify-otp": verifyOtp(request, response); break;
            case "/auth/logout": logout(request, response); break;
            default: login(request, response);
        }
    }

    private void showLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User_24133057 current = session == null ? null
                : (User_24133057) session.getAttribute("currentUser");
        if (current != null) {
            redirect(request, response, current.isAdmin() ? "/admin" : "/home");
            return;
        }
        render(request, response, "login");
    }

    private void login(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        try {
            Optional<User_24133057> authenticated = userService.authenticate(username, password);
            if (authenticated.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                Map<String, Object> model = new HashMap<>();
                model.put("error", "Sai thông tin đăng nhập hoặc tài khoản chưa được kích hoạt.");
                model.put("username", username);
                render(request, response, "login", model);
                return;
            }
            HttpSession oldSession = request.getSession(false);
            String commerceReturnPath = oldSession == null ? null
                    : (String) oldSession.getAttribute("commerceReturnPath");
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("currentUser", authenticated.get());
            newSession.setMaxInactiveInterval(30 * 60);
            if (commerceReturnPath != null && java.util.Set.of("/shop", "/cart", "/checkout", "/orders", "/videos/favorites", "/account/activate", "/account/videos").contains(commerceReturnPath)) {
                redirect(request, response, commerceReturnPath);
            } else {
                redirect(request, response, authenticated.get().isAdmin() ? "/admin" : "/home");
            }
        } catch (SQLException exception) {
            throw new ServletException("Không thể đăng nhập", exception);
        }
    }

    private void register(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User_24133057 user = new User_24133057();
        user.setUsername(request.getParameter("username"));
        user.setFullname(request.getParameter("fullname"));
        user.setEmail(request.getParameter("email"));
        user.setPhone(request.getParameter("phone"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        Map<String, Object> model = new HashMap<>();
        model.put("formUser", user);
        if (password == null || !password.equals(confirmPassword)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.put("error", "Mật khẩu xác nhận không khớp.");
            render(request, response, "register", model);
            return;
        }
        try {
            userService.register(user, password);
            String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
            boolean sent = emailService.sendRegistrationOtp(user.getEmail(), user.getFullname(), otp);
            if (!sent) {
                userService.delete(user.getUsername());
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                model.put("error", "Không thể gửi OTP đến email này. Vui lòng kiểm tra địa chỉ email hoặc cấu hình SMTP rồi thử lại.");
                render(request, response, "register", model);
                return;
            }
            HttpSession session = request.getSession(true);
            session.setAttribute("pendingUsername", user.getUsername());
            session.setAttribute("pendingEmail", user.getEmail());
            session.setAttribute("registrationOtp", otp);
            session.setAttribute("otpExpiresAt", Instant.now().plusSeconds(OTP_VALID_SECONDS).toEpochMilli());
            session.setAttribute("otpAttempts", 0);
            flash(request, "flashSuccess", "Mã OTP đã được gửi đến email của bạn.");
            redirect(request, response, "/auth/verify-otp");
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.put("error", exception.getMessage());
            render(request, response, "register", model);
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 2627 || exception.getErrorCode() == 2601) {
                response.setStatus(400); model.put("error", "Tên đăng nhập hoặc email đã được sử dụng. Vui lòng kiểm tra lại.");
                render(request, response, "register", model);
            } else throw new ServletException("Không thể đăng ký", exception);
        }
    }

    private void showOtp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("pendingUsername") == null) {
            flash(request, "flashError", "Vui lòng đăng ký tài khoản trước.");
            redirect(request, response, "/auth/register");
            return;
        }
        Map<String, Object> model = new HashMap<>();
        model.put("pendingEmail", session.getAttribute("pendingEmail"));
        render(request, response, "verify-otp", model);
    }

    private void verifyOtp(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("registrationOtp") == null) {
            flash(request, "flashError", "Phiên xác thực không tồn tại hoặc đã hết hạn.");
            redirect(request, response, "/auth/register");
            return;
        }
        long expiresAt = (Long) session.getAttribute("otpExpiresAt");
        int attempts = (Integer) session.getAttribute("otpAttempts");
        String submitted = request.getParameter("otp");
        String expected = (String) session.getAttribute("registrationOtp");
        if (Instant.now().toEpochMilli() > expiresAt || attempts >= MAX_OTP_ATTEMPTS) {
            clearOtp(session);
            flash(request, "flashError", "Mã OTP đã hết hạn. Vui lòng đăng ký lại.");
            redirect(request, response, "/auth/register");
            return;
        }
        if (submitted == null || !expected.equals(submitted.trim())) {
            session.setAttribute("otpAttempts", attempts + 1);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> model = new HashMap<>();
            model.put("pendingEmail", session.getAttribute("pendingEmail"));
            model.put("error", "Mã OTP không đúng. Bạn còn " + (MAX_OTP_ATTEMPTS - attempts - 1) + " lần thử.");
            render(request, response, "verify-otp", model);
            return;
        }
        try {
            userService.activate((String) session.getAttribute("pendingUsername"));
            clearOtp(session);
            flash(request, "flashSuccess", "Kích hoạt thành công. Bạn có thể đăng nhập ngay.");
            redirect(request, response, "/auth/login");
        } catch (SQLException exception) {
            throw new ServletException("Không thể kích hoạt tài khoản", exception);
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        redirect(request, response, "/auth/login");
    }

    private void clearOtp(HttpSession session) {
        session.removeAttribute("pendingUsername");
        session.removeAttribute("pendingEmail");
        session.removeAttribute("registrationOtp");
        session.removeAttribute("otpExpiresAt");
        session.removeAttribute("otpAttempts");
    }
}
