package huuthang.model;

public enum OrderStatus_24133057 {
    NEW("Đơn hàng mới"), CONFIRMED("Đã xác nhận"), PREPARING("Chuẩn bị hàng"),
    SHIPPING("Vận chuyển"), OUT_FOR_DELIVERY("Đang giao hàng"), DELIVERED("Đã giao"),
    CANCELLED("Đơn hàng hủy"), RETURNED("Đơn hàng hoàn");

    private final String label;
    OrderStatus_24133057(String label) { this.label = label; }
    public String getCode() { return name(); }
    public String getLabel() { return label; }
    public boolean isCancellable() { return this == NEW || this == CONFIRMED; }
    public java.util.List<OrderStatus_24133057> getNextStatuses() {
        return switch (this) {
            case NEW -> java.util.List.of(CONFIRMED, CANCELLED);
            case CONFIRMED -> java.util.List.of(PREPARING, CANCELLED);
            case PREPARING -> java.util.List.of(SHIPPING);
            case SHIPPING -> java.util.List.of(OUT_FOR_DELIVERY);
            case OUT_FOR_DELIVERY -> java.util.List.of(DELIVERED);
            case DELIVERED -> java.util.List.of(RETURNED);
            default -> java.util.List.of();
        };
    }
    public static OrderStatus_24133057 filter(String value) {
        if (value == null || value.isBlank()) return null;
        try { return valueOf(value); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ."); }
    }
}
