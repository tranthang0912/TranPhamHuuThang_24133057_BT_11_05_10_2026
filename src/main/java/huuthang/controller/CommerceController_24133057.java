package huuthang.controller;

import huuthang.config.ServiceFactory_24133057;
import huuthang.dao.CommerceDao_24133057;
import huuthang.model.*;
import huuthang.service.UserService_24133057;
import huuthang.util.CommerceValidation_24133057;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.*;

public class CommerceController_24133057 extends BaseController_24133057 {
    private static final long serialVersionUID = 1L;
    private transient CommerceDao_24133057 dao;
    private transient UserService_24133057 users;
    @Override public void init() { dao = new CommerceDao_24133057(); users = ServiceFactory_24133057.userService(); }

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        try {
            csrf(request);
            String path = request.getServletPath();
            if ("/orders/cancel".equals(path)) { response.sendError(405); return; }
            if ("/shop".equals(path)) {
                render(request, response, "shop", Map.of("products", dao.products())); return;
            }
            User_24133057 user = signedIn(request, response);
            if (user == null) return;
            switch (path) {
                case "/cart" -> render(request, response, "cart", Map.of("cart", dao.cart(user.getUsername())));
                case "/checkout" -> {
                    Cart_24133057 cart = dao.cart(user.getUsername());
                    if (!cart.isCheckoutAllowed()) {
                        flash(request, "flashError", "Kiểm tra giỏ hàng và số lượng còn bán trước khi thanh toán.");
                        redirect(request, response, "/cart"); return;
                    }
                    CheckoutDetails_24133057 form = new CheckoutDetails_24133057(user.getFullname(), user.getPhone(), "", "", "COD");
                    checkoutView(request, response, cart, form, Map.of());
                }
                case "/orders" -> history(request, response, user);
                case "/orders/detail" -> {
                    long id = CommerceValidation_24133057.orderId(request.getParameter("id"));
                    Optional<Order_24133057> order = dao.order(user.getUsername(), id);
                    if (order.isEmpty()) { problem(request, response, 404, "Đơn hàng không tồn tại."); return; }
                    huuthang.dao.ActivationDao_24133057 activation = new huuthang.dao.ActivationDao_24133057();
                    render(request, response, "order-detail", Map.of("order", order.get(),
                            "activationCodes", activation.orderCodes(user.getUsername(), id),
                            "activationEmailStatus", activation.emailState(user.getUsername(), id)));
                }
                default -> response.sendError(404);
            }
        } catch (IllegalArgumentException e) { problem(request, response, 400, e.getMessage()); }
        catch (SQLException e) { databaseProblem(request, response, e); }
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        String path = request.getServletPath();
        if (!Set.of("/cart", "/checkout", "/orders/cancel").contains(path)) { response.sendError(405); return; }
        try {
            User_24133057 user = signedIn(request, response);
            if (user == null) return;
            String expected = csrf(request), submitted = request.getParameter("csrfToken");
            if (submitted == null || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), submitted.getBytes(StandardCharsets.UTF_8))) {
                problem(request, response, 403, "Phiên thao tác không hợp lệ. Vui lòng tải lại trang và thử lại."); return;
            }
            if ("/checkout".equals(path)) { placeOrder(request, response, user); return; }
            if ("/orders/cancel".equals(path)) {
                long id = CommerceValidation_24133057.orderId(request.getParameter("id"));
                dao.cancel(user.getUsername(), id);
                flash(request, "flashSuccess", "Đã hủy đơn hàng và hoàn lại số lượng có thể đặt.");
                redirect(request, response, "/orders/detail?id=" + id); return;
            }
            String action = request.getParameter("action");
            if (action == null) throw new IllegalArgumentException("Thao tác giỏ hàng không hợp lệ.");
            switch (action) {
                case "add", "update" -> dao.changeCart(user.getUsername(),
                        CommerceValidation_24133057.productId(request.getParameter("productId")),
                        CommerceValidation_24133057.quantity(request.getParameter("quantity")), "add".equals(action));
                case "remove" -> dao.remove(user.getUsername(), CommerceValidation_24133057.productId(request.getParameter("productId")));
                case "clear" -> dao.remove(user.getUsername(), null);
                default -> throw new IllegalArgumentException("Thao tác giỏ hàng không hợp lệ.");
            }
            flash(request, "flashSuccess", "Đã cập nhật giỏ hàng."); redirect(request, response, "/cart");
        } catch (IllegalArgumentException e) {
            if ("/cart".equals(path)) {
                response.setStatus(400);
                User_24133057 user = (User_24133057) request.getSession().getAttribute("currentUser");
                try { render(request, response, "cart", Map.of("cart", dao.cart(user.getUsername()), "error", e.getMessage())); }
                catch (SQLException sql) { databaseProblem(request, response, sql); }
            } else problem(request, response, 400, e.getMessage());
        } catch (SQLException e) { databaseProblem(request, response, e); }
    }

    private void placeOrder(HttpServletRequest request, HttpServletResponse response, User_24133057 user) throws SQLException, ServletException, IOException {
        CheckoutDetails_24133057 form = new CheckoutDetails_24133057(request.getParameter("recipient"), request.getParameter("phone"),
                request.getParameter("address"), request.getParameter("note"), request.getParameter("paymentMethod"));
        String token = request.getParameter("checkoutToken");
        String fingerprint;
        synchronized (request.getSession()) { fingerprint = tokens(request.getSession()).get(token); }
        if (fingerprint == null) throw new IllegalArgumentException("Phiên thanh toán đã hết hạn. Vui lòng mở lại trang thanh toán.");
        Map<String, String> errors = form.validate();
        if (!errors.isEmpty()) {
            response.setStatus(400); checkoutView(request, response, dao.cart(user.getUsername()), form, errors); return;
        }
        try {
            long id = dao.checkout(user.getUsername(), token, fingerprint, form);
            flash(request, "flashSuccess", "Đặt đơn COD thành công. Mã kích hoạt video sẽ được cấp sau khi giao hàng và thu đủ tiền.");
            redirect(request, response, "/orders/detail?id=" + id);
        } catch (IllegalArgumentException e) {
            response.setStatus(409);
            checkoutView(request, response, dao.cart(user.getUsername()), form, Map.of("cart", e.getMessage()));
        }
    }
    private void checkoutView(HttpServletRequest request, HttpServletResponse response, Cart_24133057 cart,
                              CheckoutDetails_24133057 form, Map<String, String> errors) throws ServletException, IOException {
        String token = UUID.randomUUID().toString();
        synchronized (request.getSession()) {
            Map<String, String> tokens = tokens(request.getSession());
            if (tokens.size() >= 8) tokens.remove(tokens.keySet().iterator().next());
            tokens.put(token, cart.fingerprint());
        }
        render(request, response, "checkout", Map.of("cart", cart, "form", form, "errors", errors, "checkoutToken", token));
    }
    @SuppressWarnings("unchecked") private Map<String, String> tokens(HttpSession session) {
        Map<String, String> tokens = (Map<String, String>) session.getAttribute("checkoutTokens");
        if (tokens == null) { tokens = new LinkedHashMap<>(); session.setAttribute("checkoutTokens", tokens); }
        return tokens;
    }
    private void history(HttpServletRequest request, HttpServletResponse response, User_24133057 user) throws SQLException, ServletException, IOException {
        OrderStatus_24133057 status = OrderStatus_24133057.filter(request.getParameter("status"));
        int page = CommerceValidation_24133057.page(request.getParameter("page"));
        Map<String, Integer> counts = dao.counts(user.getUsername());
        int total = counts.get(status == null ? "ALL" : status.name());
        int pages = Math.max(1, (total + 9) / 10);
        if (page > pages) { problem(request, response, 404, "Trang lịch sử đơn hàng không tồn tại."); return; }
        Map<String, Object> model = new HashMap<>();
        model.put("orders", dao.orders(user.getUsername(), status, page)); model.put("statuses", OrderStatus_24133057.values());
        model.put("selectedStatus", status == null ? "" : status.name()); model.put("counts", counts);
        model.put("pageNumber", page); model.put("totalPages", pages); model.put("totalOrders", total);
        render(request, response, "orders", model);
    }
    private String csrf(HttpServletRequest request) {
        HttpSession session = request.getSession();
        synchronized (session) {
            if (session.getAttribute("commerceCsrf") == null) session.setAttribute("commerceCsrf", UUID.randomUUID().toString());
            String token = (String) session.getAttribute("commerceCsrf"); request.setAttribute("csrfToken", token); return token;
        }
    }
    private User_24133057 signedIn(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
        HttpSession session = request.getSession(false);
        User_24133057 user = session == null ? null : (User_24133057) session.getAttribute("currentUser");
        if (user == null) {
            if ("GET".equals(request.getMethod())) request.getSession().setAttribute("commerceReturnPath", request.getServletPath());
            flash(request, "flashError", "Vui lòng đăng nhập để sử dụng giỏ hàng và xem đơn hàng.");
            redirect(request, response, "/auth/login"); return null;
        }
        Optional<User_24133057> fresh = users.findByUsername(user.getUsername());
        if (fresh.isEmpty() || !fresh.get().isActive()) {
            session.invalidate(); flash(request, "flashError", "Tài khoản đã bị khóa. Vui lòng đăng nhập lại.");
            redirect(request, response, "/auth/login"); return null;
        }
        session.setAttribute("currentUser", fresh.get()); return fresh.get();
    }
    private void databaseProblem(HttpServletRequest request, HttpServletResponse response, SQLException e) throws ServletException, IOException {
        getServletContext().log("Commerce database operation failed", e);
        problem(request, response, 503, "Cửa hàng đang tạm thời không khả dụng. Vui lòng thử lại sau; nếu đã đặt đơn, hãy kiểm tra lịch sử trước khi đặt lại.");
    }
    private void problem(HttpServletRequest request, HttpServletResponse response, int status, String message) throws ServletException, IOException {
        response.setStatus(status); render(request, response, "shop-error", Map.of("error", message));
    }
}
