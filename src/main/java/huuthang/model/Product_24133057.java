package huuthang.model;

import java.math.BigDecimal;

public class Product_24133057 {
    private final int productId;
    private final String videoId, title, description, poster;
    private final BigDecimal price;
    private final int stock;
    private final boolean active;

    public Product_24133057(int productId, String videoId, String title, String description,
                           String poster, BigDecimal price, int stock, boolean active) {
        this.productId = productId; this.videoId = videoId; this.title = title;
        this.description = description; this.poster = poster; this.price = price;
        this.stock = stock; this.active = active;
    }
    public int getProductId() { return productId; }
    public String getVideoId() { return videoId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPoster() { return poster; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isActive() { return active; }
    public int getMaxQuantity() { return Math.min(99, stock); }
}
