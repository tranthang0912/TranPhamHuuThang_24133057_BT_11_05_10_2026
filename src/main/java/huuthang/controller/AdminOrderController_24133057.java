package huuthang.controller;

import huuthang.dao.ActivationDao_24133057;
import huuthang.dao.CommerceDao_24133057;
import huuthang.model.User_24133057;
import huuthang.model.OrderStatus_24133057;
import huuthang.util.CommerceValidation_24133057;
import huuthang.util.SessionAction_24133057;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AdminOrderController_24133057 extends BaseController_24133057 {
    private static final long serialVersionUID = 1L;
    private final CommerceDao_24133057 dao = new CommerceDao_24133057();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Map<String, Object> model = new HashMap<>();
            model.put("csrfToken", SessionAction_24133057.token(request));
            OrderStatus_24133057 status = OrderStatus_24133057.filter(request.getParameter("status"));
            model.put("statuses", OrderStatus_24133057.values());
            model.put("selectedStatus", status == null ? "" : status.name());
            if (request.getParameter("id") == null) model.put("orders", dao.adminOrders(status));
            else {
                var found = dao.adminOrder(CommerceValidation_24133057.orderId(request.getParameter("id")));
                if (found.isEmpty()) { response.sendError(404); return; }
                var order = found.get();
                model.put("order", order);
                model.put("activationCodes", new ActivationDao_24133057().orderCodes(order.getUsername(), order.getOrderId()));
            }
            render(request, response, "admin-orders", model);
        } catch (IllegalArgumentException e) { response.sendError(400, e.getMessage()); }
        catch (SQLException e) { getServletContext().log("Cannot load admin orders", e); response.sendError(503); }
    }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionAction_24133057.valid(request)) { response.sendError(403); return; }
        long id;
        try { id = CommerceValidation_24133057.orderId(request.getParameter("id")); }
        catch (IllegalArgumentException e) { response.sendError(400, e.getMessage()); return; }
        User_24133057 admin = (User_24133057) request.getSession().getAttribute("currentUser");
        if (admin == null || !admin.isAdmin()) { response.sendError(403); return; }
        try {
            String action = request.getParameter("action");
            dao.adminProcess(admin.getUsername(), id, action);
            flash(request, "flashSuccess", "confirm".equals(action) ? "Đã xác nhận đơn hàng. Tiếp theo nhấn Đã thu tiền COD — cấp mã."
                    : "collect-cod".equals(action) || "DELIVERED".equals(action) ? "Đã hoàn tất COD. Mã video ở bên dưới; khách hàng cũng thấy mã trong chi tiết đơn."
                    : "RETURNED".equals(action) ? "Đã hoàn đơn, hoàn số lượng và thu hồi mã cùng quyền xem video."
                    : "CANCELLED".equals(action) ? "Đã hủy đơn và hoàn số lượng."
                    : "Đã cập nhật trạng thái đơn hàng.");
        } catch (IllegalArgumentException e) { flash(request, "flashError", e.getMessage()); }
        catch (SQLException e) { getServletContext().log("Cannot process admin order", e); response.sendError(503); return; }
        redirect(request, response, "/admin/orders?id=" + id);
    }
}
