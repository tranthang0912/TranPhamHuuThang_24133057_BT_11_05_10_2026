package huuthang.util;

public final class CommerceValidation_24133057 {
    private CommerceValidation_24133057() { }
    public static int quantity(String value) { return (int) integer(value, 99, "Số lượng phải là số nguyên từ 1 đến 99."); }
    public static int productId(String value) { return (int) integer(value, Integer.MAX_VALUE, "Mã sản phẩm không hợp lệ."); }
    public static long orderId(String value) { return integer(value, Long.MAX_VALUE, "Mã đơn hàng không hợp lệ."); }
    public static int page(String value) { return value == null ? 1 : (int) integer(value, 100000, "Trang không hợp lệ."); }
    private static long integer(String value, long max, String message) {
        if (value == null || !value.matches("[0-9]{1,19}")) throw new IllegalArgumentException(message);
        try {
            long number = Long.parseLong(value);
            if (number < 1 || number > max) throw new IllegalArgumentException(message);
            return number;
        } catch (NumberFormatException e) { throw new IllegalArgumentException(message); }
    }
}
