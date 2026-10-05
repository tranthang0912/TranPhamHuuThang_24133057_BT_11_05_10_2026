package huuthang.service.impl;

import huuthang.service.EmailService_24133057;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailServiceImpl_24133057 implements EmailService_24133057 {
    private static final Logger LOGGER = Logger.getLogger(EmailServiceImpl_24133057.class.getName());

    @Override
    public boolean sendRegistrationOtp(String recipient, String fullname, String otp) {
        return send(recipient, "Mã OTP kích hoạt tài khoản - Hữu Thắng Video", buildHtml(fullname, otp));
    }

    @Override public boolean isConfigured() {
        Properties config = loadConfiguration();
        return !configuredValue("SMTP_USERNAME", config, "smtp.username", "").isBlank()
                && !configuredValue("SMTP_PASSWORD", config, "smtp.password", "").isBlank();
    }

    @Override public boolean sendVideoActivationCodes(String recipient, String fullname, long orderId,
            java.util.List<huuthang.model.ActivationCode_24133057> codes, String activationUrl) {
        return !codes.isEmpty() && send(recipient, "Mã kích hoạt video cho đơn #" + orderId + " - Hữu Thắng", activationHtml(fullname, orderId, codes, activationUrl));
    }

    public String activationHtml(String fullname, long orderId,
            java.util.List<huuthang.model.ActivationCode_24133057> codes, String activationUrl) {
        StringBuilder body = new StringBuilder("<div style='font-family:Arial,sans-serif;max-width:620px;margin:auto;padding:28px'><h2 style='color:#5b4bdb'>Video của bạn đã sẵn sàng</h2>");
        body.append("<p>Xin chào ").append(escapeHtml(fullname)).append(",</p><p>Đơn #").append(orderId)
                .append(" đã hoàn tất thanh toán COD. Dưới đây là mã kích hoạt video:</p>");
        for (var code : codes) body.append("<div style='padding:16px;background:#f5f3ff;border-radius:12px;margin:12px 0'><b>")
                .append(escapeHtml(code.getTitle())).append("</b><p style='font-family:monospace;overflow-wrap:anywhere;font-size:16px'>")
                .append(escapeHtml(code.getCode())).append("</p></div>");
        return body.append("<p>Đăng nhập, mở <a href='").append(escapeHtml(activationUrl))
                .append("'>trang kích hoạt video</a> và nhập mã. Mỗi mã dùng cho một tài khoản; mã đã kích hoạt không thể chuyển sang tài khoản khác.</p>")
                .append("<p>Bạn cũng có thể lấy mã trong chi tiết đơn hàng. Nếu đơn được hoàn, mã và quyền xem tương ứng sẽ bị thu hồi.</p></div>").toString();
    }

    private boolean send(String recipient, String subject, String html) {
        Properties configuration = loadConfiguration();
        String username = configuredValue("SMTP_USERNAME", configuration, "smtp.username", "");
        String password = configuredValue("SMTP_PASSWORD", configuration, "smtp.password", "");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            LOGGER.warning("SMTP chưa cấu hình; chưa thể gửi email.");
            return false;
        }

        String host = configuredValue("SMTP_HOST", configuration, "smtp.host", "smtp.gmail.com");
        String port = configuredValue("SMTP_PORT", configuration, "smtp.port", "587");
        String sender = configuredValue("SMTP_FROM", configuration, "smtp.from", username);

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.connectiontimeout", "8000");
        properties.put("mail.smtp.timeout", "8000");
        properties.put("mail.smtp.writetimeout", "8000");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject, "UTF-8");
            message.setContent(html, "text/html; charset=UTF-8");
            Transport.send(message);
            return true;
        } catch (MessagingException exception) {
            LOGGER.log(Level.WARNING, "Không thể gửi email. Kiểm tra kết nối SMTP.", exception);
            return false;
        }
    }

    private String buildHtml(String fullname, String otp) {
        return "<div style='font-family:Arial,sans-serif;max-width:560px;margin:auto;padding:28px'>"
                + "<h2 style='color:#5b4bdb'>Kích hoạt tài khoản</h2>"
                + "<p>Xin chào " + escapeHtml(fullname) + ",</p>"
                + "<p>Mã OTP của bạn là:</p>"
                + "<div style='font-size:32px;font-weight:700;letter-spacing:8px;color:#111827'>"
                + escapeHtml(otp) + "</div>"
                + "<p>Mã có hiệu lực trong 5 phút. Không chia sẻ mã này với người khác.</p>"
                + "</div>";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "bạn";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private Properties loadConfiguration() {
        Properties configuration = new Properties();
        try (InputStream input = EmailServiceImpl_24133057.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                configuration.load(input);
            }
        } catch (IOException exception) {
            LOGGER.log(Level.WARNING, "Không thể đọc application.properties", exception);
        }
        return configuration;
    }

    private String configuredValue(String environmentName, Properties configuration,
                                   String propertyName, String fallback) {
        String environmentValue = System.getenv(environmentName);
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }
        String propertyValue = configuration.getProperty(propertyName);
        return propertyValue == null || propertyValue.isBlank() ? fallback : propertyValue.trim();
    }
}
