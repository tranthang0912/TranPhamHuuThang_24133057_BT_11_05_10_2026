package huuthang.model;

import java.math.BigDecimal;

public class CartLine_24133057 {
    private final Product_24133057 product;
    private final int quantity;
    public CartLine_24133057(Product_24133057 product, int quantity) {
        this.product = product; this.quantity = quantity;
    }
    public Product_24133057 getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return product.getPrice().multiply(BigDecimal.valueOf(quantity)); }
    public boolean isAvailable() {
        return product.isActive() && quantity >= 1 && quantity <= product.getMaxQuantity();
    }
}
