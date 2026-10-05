package huuthang.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class CheckoutDetails_24133057 {
    private final String recipient, phone, address, note, paymentMethod;
    public CheckoutDetails_24133057(String recipient, String phone, String address, String note, String paymentMethod) {
        this.recipient = clean(recipient); this.phone = clean(phone).replaceAll("[ .()-]", "");
        this.address = clean(address).replaceAll("[\\r\\n\\t]+", " "); this.note = clean(note); this.paymentMethod = clean(paymentMethod);
    }
    private static String clean(String text) { return text == null ? "" : text.strip(); }
    public String getRecipient() { return recipient; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getNote() { return note; }
    public String getPaymentMethod() { return paymentMethod; }
    public Map<String, String> validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (recipient.length() < 2 || recipient.length() > 100
                || !recipient.matches("[\\p{L}\\p{M} .'-]+") || recipient.codePoints().filter(Character::isLetter).count() < 2)
            errors.put("recipient", "Họ tên phải có 2–100 ký tự, chỉ gồm chữ, khoảng trắng, dấu chấm, gạch nối hoặc dấu nháy.");
        if (!phone.matches("(?:0|\\+84)[35789][0-9]{8}"))
            errors.put("phone", "Nhập số di động Việt Nam hợp lệ, ví dụ 0912345678 hoặc +84912345678.");
        if (address.length() < 10 || address.length() > 500 || address.chars().anyMatch(Character::isISOControl)
                || address.codePoints().filter(Character::isLetterOrDigit).count() < 5)
            errors.put("address", "Địa chỉ phải có 10–500 ký tự, ghi rõ số nhà/đường, phường/xã và tỉnh/thành phố.");
        if (note.length() > 500 || note.chars().anyMatch(c -> Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t'))
            errors.put("note", "Ghi chú tối đa 500 ký tự và không chứa ký tự điều khiển.");
        if (!"COD".equals(paymentMethod)) errors.put("paymentMethod", "Chỉ hỗ trợ thanh toán khi nhận hàng (COD).");
        return errors;
    }
}
