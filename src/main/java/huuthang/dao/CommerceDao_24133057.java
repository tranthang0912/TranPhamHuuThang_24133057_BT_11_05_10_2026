package huuthang.dao;

import huuthang.config.DatabaseConnection_24133057;
import huuthang.model.*;

import java.sql.*;
import java.util.*;

public class CommerceDao_24133057 {
    private static final String PRODUCT_SELECT = "SELECT p.ProductId,p.VideoId,v.Title,v.Description,v.Poster,p.Price,p.Stock,"
            + "CASE WHEN p.Active=1 AND v.Active=1 AND c.Status=1 THEN 1 ELSE 0 END AS SaleActive "
            + "FROM dbo.Products p%s JOIN dbo.Videos v ON v.VideoId=p.VideoId "
            + "LEFT JOIN dbo.Category c ON c.CategoryId=v.CategoryId ";
    public List<Product_24133057> products() throws SQLException {
        try (Connection c = DatabaseConnection_24133057.getConnection();
             PreparedStatement s = c.prepareStatement(String.format(PRODUCT_SELECT, "")
                     + "WHERE p.Active=1 AND v.Active=1 AND c.Status=1 ORDER BY p.ProductId");
             ResultSet rs = s.executeQuery()) {
            List<Product_24133057> products = new ArrayList<>();
            while (rs.next()) products.add(product(rs));
            return products;
        }
    }
    public Cart_24133057 cart(String username) throws SQLException {
        try (Connection c = DatabaseConnection_24133057.getConnection()) { return cart(c, username, false); }
    }
    private Cart_24133057 cart(Connection c, String username, boolean lock) throws SQLException {
        List<int[]> entries = new ArrayList<>();
        try (PreparedStatement s = c.prepareStatement("SELECT ProductId,Quantity FROM dbo.CartItems "
                + (lock ? "WITH (UPDLOCK,HOLDLOCK) " : "") + "WHERE Username=? ORDER BY ProductId")) {
            s.setNString(1, username);
            try (ResultSet rs = s.executeQuery()) { while (rs.next()) entries.add(new int[]{rs.getInt(1), rs.getInt(2)}); }
        }
        List<CartLine_24133057> lines = new ArrayList<>();
        // All checkouts acquire product locks in ascending ID order to avoid lock inversion.
        for (int[] entry : entries) lines.add(new CartLine_24133057(findProduct(c, entry[0], lock), entry[1]));
        return new Cart_24133057(lines);
    }
    private Product_24133057 findProduct(Connection c, int id, boolean lock) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(String.format(PRODUCT_SELECT,
                lock ? " WITH (UPDLOCK,HOLDLOCK)" : "") + "WHERE p.ProductId=?")) {
            s.setInt(1, id);
            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Video không tồn tại.");
                return product(rs);
            }
        }
    }
    private Product_24133057 product(ResultSet rs) throws SQLException {
        return new Product_24133057(rs.getInt("ProductId"), rs.getNString("VideoId"), rs.getNString("Title"),
                rs.getNString("Description"), rs.getNString("Poster"), rs.getBigDecimal("Price"),
                rs.getInt("Stock"), rs.getBoolean("SaleActive"));
    }
    private void lockUser(Connection c, String username) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT Active FROM dbo.Users WITH (UPDLOCK,HOLDLOCK) WHERE Username=?")) {
            s.setNString(1, username);
            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next() || !rs.getBoolean(1)) throw new IllegalArgumentException("Tài khoản không tồn tại hoặc đã bị khóa.");
            }
        }
    }
    public void changeCart(String username, int productId, int quantity, boolean add) throws SQLException {
        if (quantity < 1 || quantity > 99) throw new IllegalArgumentException("Số lượng phải từ 1 đến 99.");
        transaction(c -> {
            lockUser(c, username);
            int existing = 0;
            try (PreparedStatement s = c.prepareStatement("SELECT Quantity FROM dbo.CartItems WHERE Username=? AND ProductId=?")) {
                s.setNString(1, username); s.setInt(2, productId);
                try (ResultSet rs = s.executeQuery()) { if (rs.next()) existing = rs.getInt(1); }
            }
            if (!add && existing == 0) throw new IllegalArgumentException("Video không có trong giỏ hàng.");
            if (existing == 0) {
                try (PreparedStatement s = c.prepareStatement("SELECT COUNT(*) FROM dbo.CartItems WHERE Username=?")) {
                    s.setNString(1, username);
                    try (ResultSet rs = s.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) >= 50) throw new IllegalArgumentException("Giỏ hàng tối đa 50 video khác nhau.");
                    }
                }
            }
            Product_24133057 p = findProduct(c, productId, true);
            int target = add ? existing + quantity : quantity;
            if (!p.isActive()) throw new IllegalArgumentException("Video đã ngừng bán. Vui lòng xóa khỏi giỏ hàng.");
            if (target > 99) throw new IllegalArgumentException("Mỗi video được đặt tối đa 99 bộ.");
            if (target > p.getStock()) throw new IllegalArgumentException("Video chỉ còn " + p.getStock() + " bộ có thể đặt.");
            String sql = existing == 0 ? "INSERT dbo.CartItems(Quantity,Username,ProductId) VALUES(?,?,?)"
                    : "UPDATE dbo.CartItems SET Quantity=?,UpdatedAt=SYSUTCDATETIME() WHERE Username=? AND ProductId=?";
            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, target); s.setNString(2, username); s.setInt(3, productId); s.executeUpdate();
            }
            return null;
        });
    }
    public void remove(String username, Integer productId) throws SQLException {
        transaction(c -> {
            lockUser(c, username);
            try (PreparedStatement s = c.prepareStatement("DELETE dbo.CartItems WHERE Username=?"
                    + (productId == null ? "" : " AND ProductId=?"))) {
                s.setNString(1, username);
                if (productId != null) s.setInt(2, productId);
                int removed = s.executeUpdate();
                if (productId != null && removed == 0) throw new IllegalArgumentException("Video không có trong giỏ hàng.");
            }
            return null;
        });
    }
    public long checkout(String username, String token, String fingerprint, CheckoutDetails_24133057 details) throws SQLException {
        if (!details.validate().isEmpty()) throw new IllegalArgumentException("Thông tin nhận hàng không hợp lệ.");
        UUID.fromString(token);
        return transaction(c -> {
            lockUser(c, username);
            // Retrying an already committed submission returns the original order.
            try (PreparedStatement s = c.prepareStatement("SELECT OrderId FROM dbo.Orders WHERE Username=? AND CheckoutToken=?")) {
                s.setNString(1, username); s.setString(2, token);
                try (ResultSet rs = s.executeQuery()) { if (rs.next()) return rs.getLong(1); }
            }
            Cart_24133057 cart = cart(c, username, true);
            if (!cart.isCheckoutAllowed()) throw new IllegalArgumentException("Giỏ hàng trống hoặc có video hết hàng/ngừng bán. Vui lòng kiểm tra lại.");
            if (!cart.fingerprint().equals(fingerprint))
                throw new IllegalArgumentException("Giỏ hàng hoặc giá đã thay đổi. Vui lòng mở lại trang thanh toán để xác nhận.");
            long orderId;
            String sql = "INSERT dbo.Orders(Username,CheckoutToken,Recipient,Phone,Address,Note,Total) VALUES(?,?,?,?,?,?,?)";
            try (PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                s.setNString(1, username); s.setString(2, token); s.setNString(3, details.getRecipient());
                s.setString(4, details.getPhone()); s.setNString(5, details.getAddress());
                s.setNString(6, details.getNote()); s.setBigDecimal(7, cart.getTotal()); s.executeUpdate();
                try (ResultSet rs = s.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("Không nhận được mã đơn hàng.");
                    orderId = rs.getLong(1);
                }
            }
            for (CartLine_24133057 line : cart.getLines()) {
                Product_24133057 p = line.getProduct();
                try (PreparedStatement s = c.prepareStatement("UPDATE dbo.Products SET Stock=Stock-? WHERE ProductId=? AND Stock>=? AND Active=1")) {
                    s.setInt(1, line.getQuantity()); s.setInt(2, p.getProductId()); s.setInt(3, line.getQuantity());
                    if (s.executeUpdate() != 1) throw new IllegalArgumentException("Tồn kho đã thay đổi. Vui lòng thử lại.");
                }
                try (PreparedStatement s = c.prepareStatement("INSERT dbo.OrderItems(OrderId,ProductId,VideoTitle,UnitPrice,Quantity) VALUES(?,?,?,?,?)")) {
                    s.setLong(1, orderId); s.setInt(2, p.getProductId()); s.setNString(3, p.getTitle());
                    s.setBigDecimal(4, p.getPrice()); s.setInt(5, line.getQuantity()); s.executeUpdate();
                }
            }
            try (PreparedStatement s = c.prepareStatement("DELETE dbo.CartItems WHERE Username=?")) {
                s.setNString(1, username); s.executeUpdate();
            }
            return orderId;
        });
    }
    public void cancel(String username, long orderId) throws SQLException {
        transaction(c -> {
            lockUser(c, username);
            try (PreparedStatement s = c.prepareStatement("UPDATE dbo.Orders SET Status='CANCELLED' "
                    + "WHERE Username=? AND OrderId=? AND Status IN ('NEW','CONFIRMED')")) {
                s.setNString(1, username); s.setLong(2, orderId);
                if (s.executeUpdate() != 1)
                    throw new IllegalArgumentException("Không thể hủy đơn: đơn không tồn tại hoặc đã được chuẩn bị/giao hàng.");
            }
            return null;
        });
    }
    public Map<String, Integer> counts(String username) throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (OrderStatus_24133057 status : OrderStatus_24133057.values()) counts.put(status.name(), 0);
        int total = 0;
        try (Connection c = DatabaseConnection_24133057.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT Status,COUNT(*) FROM dbo.Orders WHERE Username=? GROUP BY Status")) {
            s.setNString(1, username);
            try (ResultSet rs = s.executeQuery()) {
                while (rs.next()) { counts.put(rs.getString(1), rs.getInt(2)); total += rs.getInt(2); }
            }
        }
        counts.put("ALL", total); return counts;
    }
    public List<Order_24133057> orders(String username, OrderStatus_24133057 status, int page) throws SQLException {
        try (Connection c = DatabaseConnection_24133057.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT * FROM dbo.Orders WHERE Username=?"
                     + (status == null ? "" : " AND Status=?")
                     + " ORDER BY CreatedAt DESC,OrderId DESC OFFSET ? ROWS FETCH NEXT 10 ROWS ONLY")) {
            int param = 1; s.setNString(param++, username);
            if (status != null) s.setString(param++, status.name());
            s.setInt(param, (page - 1) * 10);
            List<Order_24133057> orders = new ArrayList<>();
            try (ResultSet rs = s.executeQuery()) { while (rs.next()) orders.add(order(rs)); }
            return orders;
        }
    }
    public Optional<Order_24133057> order(String username, long id) throws SQLException {
        try (Connection c = DatabaseConnection_24133057.getConnection()) {
            Order_24133057 order;
            try (PreparedStatement s = c.prepareStatement("SELECT * FROM dbo.Orders WHERE Username=? AND OrderId=?")) {
                s.setNString(1, username); s.setLong(2, id);
                try (ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    order = order(rs);
                }
            }
            List<Order_24133057.Item> items = new ArrayList<>();
            try (PreparedStatement s = c.prepareStatement("SELECT VideoTitle,UnitPrice,Quantity FROM dbo.OrderItems WHERE OrderId=? ORDER BY ProductId")) {
                s.setLong(1, id);
                try (ResultSet rs = s.executeQuery()) {
                    while (rs.next()) items.add(new Order_24133057.Item(rs.getNString(1), rs.getBigDecimal(2), rs.getInt(3)));
                }
            }
            order.setItems(items);
            List<Order_24133057.Event> events = new ArrayList<>();
            try (PreparedStatement s = c.prepareStatement("SELECT Status,ChangedAt FROM dbo.OrderStatusHistory WHERE OrderId=? ORDER BY HistoryId")) {
                s.setLong(1, id);
                try (ResultSet rs = s.executeQuery()) {
                    while (rs.next()) events.add(new Order_24133057.Event(rs.getString(1), utc(rs, "ChangedAt")));
                }
            }
            order.setEvents(events); return Optional.of(order);
        }
    }
    public List<Order_24133057> adminOrders(OrderStatus_24133057 status) throws SQLException {
        try (Connection c = DatabaseConnection_24133057.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT TOP (50) * FROM dbo.Orders"
                     + (status == null ? "" : " WHERE Status=?") + " ORDER BY CreatedAt DESC,OrderId DESC")) {
            if (status != null) s.setString(1, status.name());
            List<Order_24133057> result = new ArrayList<>();
            try (ResultSet rs = s.executeQuery()) { while (rs.next()) result.add(order(rs)); }
            return result;
        }
    }
    public Optional<Order_24133057> adminOrder(long id) throws SQLException {
        String owner;
        try (Connection c = DatabaseConnection_24133057.getConnection();
             PreparedStatement s = c.prepareStatement("SELECT Username FROM dbo.Orders WHERE OrderId=?")) {
            s.setLong(1, id);
            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                owner = rs.getNString(1);
            }
        }
        return order(owner, id);
    }
    public void adminProcess(String admin, long id, String action) throws SQLException {
        if (id <= 0 || !Set.of("confirm", "collect-cod", "CONFIRMED", "PREPARING", "SHIPPING",
                "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED", "RETURNED").contains(action == null ? "" : action))
            throw new IllegalArgumentException("Thao tác hoặc mã đơn hàng không hợp lệ.");
        transaction(c -> {
            try (PreparedStatement s = c.prepareStatement("SELECT 1 FROM dbo.Users WITH (UPDLOCK,HOLDLOCK) WHERE Username=? AND Active=1 AND Admin=1")) {
                s.setNString(1, admin);
                try (ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Chỉ quản trị viên đang hoạt động được xử lý đơn hàng.");
                }
            }
            String state;
            try (PreparedStatement s = c.prepareStatement("SELECT Status FROM dbo.Orders WITH (UPDLOCK,HOLDLOCK) WHERE OrderId=?")) {
                s.setLong(1, id);
                try (ResultSet rs = s.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Đơn hàng không tồn tại.");
                    state = rs.getString(1);
                }
            }
            if ("confirm".equals(action)) {
                if ("CONFIRMED".equals(state)) return null;
                if (!"NEW".equals(state)) throw new IllegalArgumentException("Chỉ xác nhận được đơn hàng mới.");
                setAdminStatus(c, id, "CONFIRMED");
            } else if ("collect-cod".equals(action)) {
                List<String> flow = List.of("CONFIRMED", "PREPARING", "SHIPPING", "OUT_FOR_DELIVERY", "DELIVERED");
                int start = flow.indexOf(state);
                if (start < 0) throw new IllegalArgumentException("Cần xác nhận đơn trước. Đơn đã hủy/hoàn không được cấp mã.");
                // Follow every existing transition atomically; the delivery trigger issues codes once.
                for (int i = start + 1; i < flow.size(); i++) setAdminStatus(c, id, flow.get(i));
            } else {
                if (state.equals(action)) return null;
                if (!OrderStatus_24133057.valueOf(state).getNextStatuses().contains(OrderStatus_24133057.valueOf(action)))
                    throw new IllegalArgumentException("Không thể chuyển trạng thái này. Vui lòng tải lại đơn và chọn bước tiếp theo.");
                setAdminStatus(c, id, action);
            }
            return null;
        });
    }
    private void setAdminStatus(Connection c, long id, String state) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("UPDATE dbo.Orders SET Status=? WHERE OrderId=?")) {
            s.setString(1, state); s.setLong(2, id); s.executeUpdate();
        }
    }
    private Timestamp utc(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column, Calendar.getInstance(TimeZone.getTimeZone("UTC")));
    }
    private Order_24133057 order(ResultSet rs) throws SQLException {
        Order_24133057 result = new Order_24133057(rs.getLong("OrderId"), utc(rs, "CreatedAt"), rs.getString("Status"),
                rs.getBigDecimal("Total"), rs.getNString("Recipient"), rs.getString("Phone"),
                rs.getNString("Address"), rs.getNString("Note"), rs.getString("PaymentStatus"));
        result.setUsername(rs.getNString("Username"));
        return result;
    }
    private <T> T transaction(SqlWork<T> work) throws SQLException {
        try (Connection c = DatabaseConnection_24133057.getConnection()) {
            c.setAutoCommit(false);
            try { T result = work.run(c); c.commit(); return result; }
            catch (SQLException | RuntimeException e) {
                try { c.rollback(); } catch (SQLException rollback) { e.addSuppressed(rollback); }
                throw e;
            }
        }
    }
    @FunctionalInterface private interface SqlWork<T> { T run(Connection c) throws SQLException; }
}
