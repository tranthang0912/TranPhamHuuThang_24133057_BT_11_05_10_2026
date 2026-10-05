package huuthang.model;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

public class Cart_24133057 {
    private final List<CartLine_24133057> lines;
    public Cart_24133057(List<CartLine_24133057> lines) { this.lines = List.copyOf(lines); }
    public List<CartLine_24133057> getLines() { return lines; }
    public boolean isEmpty() { return lines.isEmpty(); }
    public int getQuantity() { return lines.stream().mapToInt(CartLine_24133057::getQuantity).sum(); }
    public BigDecimal getTotal() {
        return lines.stream().map(CartLine_24133057::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public boolean isCheckoutAllowed() { return !isEmpty() && lines.size() <= 50 && lines.stream().allMatch(CartLine_24133057::isAvailable); }
    // The server keeps this digest with the checkout token to detect a stale cart or price.
    public String fingerprint() {
        StringBuilder text = new StringBuilder();
        lines.stream().sorted(java.util.Comparator.comparingInt(l -> l.getProduct().getProductId()))
                .forEach(l -> text.append(l.getProduct().getProductId()).append(':')
                        .append(l.getQuantity()).append(':').append(l.getProduct().getPrice().toPlainString()).append(';'));
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(text.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
}
