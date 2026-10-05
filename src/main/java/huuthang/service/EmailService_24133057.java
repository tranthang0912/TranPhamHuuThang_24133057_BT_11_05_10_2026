package huuthang.service;

public interface EmailService_24133057 {
    boolean sendRegistrationOtp(String recipient, String fullname, String otp);
    boolean isConfigured();
    boolean sendVideoActivationCodes(String recipient, String fullname, long orderId,
            java.util.List<huuthang.model.ActivationCode_24133057> codes, String activationUrl);
}
