package huuthang.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

public class Order_24133057 {
    private final long orderId;
    private final Timestamp createdAt;
    private final OrderStatus_24133057 status;
    private final BigDecimal total;
    private final String recipient, phone, address, note, paymentStatus;
    private List<Item> items = List.of();
    private List<Event> events = List.of();
    private String username;
    public Order_24133057(long orderId, Timestamp createdAt, String status, BigDecimal total,
                         String recipient, String phone, String address, String note, String paymentStatus) {
        this.orderId = orderId; this.createdAt = createdAt; this.status = OrderStatus_24133057.valueOf(status);
        this.total = total; this.recipient = recipient; this.phone = phone;
        this.address = address; this.note = note; this.paymentStatus = paymentStatus;
    }
    public long getOrderId() { return orderId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Timestamp getCreatedAt() { return createdAt; }
    public OrderStatus_24133057 getStatus() { return status; }
    public BigDecimal getTotal() { return total; }
    public String getRecipient() { return recipient; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getNote() { return note; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getPaymentLabel() {
        return switch (paymentStatus) {
            case "PAID" -> "Đã thu tiền COD";
            case "VOID" -> "Không thu tiền — đơn đã hủy";
            case "REFUNDED" -> "Đã hoàn tiền";
            default -> "Chờ thanh toán khi nhận hàng";
        };
    }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = List.copyOf(items); }
    public List<Event> getEvents() { return events; }
    public void setEvents(List<Event> events) { this.events = List.copyOf(events); }
    public static class Item {
        private final String title;
        private final BigDecimal unitPrice;
        private final int quantity;
        public Item(String title, BigDecimal unitPrice, int quantity) {
            this.title = title; this.unitPrice = unitPrice; this.quantity = quantity;
        }
        public String getTitle() { return title; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public BigDecimal getSubtotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    }
    public static class Event {
        private final OrderStatus_24133057 status;
        private final Timestamp changedAt;
        public Event(String status, Timestamp changedAt) {
            this.status = OrderStatus_24133057.valueOf(status); this.changedAt = changedAt;
        }
        public OrderStatus_24133057 getStatus() { return status; }
        public Timestamp getChangedAt() { return changedAt; }
    }
}
