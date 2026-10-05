package huuthang.controller;

import huuthang.dao.ActivationDao_24133057;
import huuthang.model.User_24133057;
import huuthang.util.SessionAction_24133057;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ActivationController_24133057 extends BaseController_24133057 {
    private final ActivationDao_24133057 dao = new ActivationDao_24133057();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        try {
            User_24133057 user = activeUser(request, response); if (user == null) return;
            if ("/account/videos".equals(request.getServletPath())) render(request, response, "my-videos", Map.of("videos", dao.library(user.getUsername())));
            else render(request, response, "activate", Map.of("videoCsrf", SessionAction_24133057.token(request)));
        } catch (SQLException e) { problem(request, response, 503, "Chưa tải được thư viện video. Vui lòng thử lại."); getServletContext().log("Activation page failed", e); }
    }
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        if (!"/account/activate".equals(request.getServletPath())) { response.sendError(405); return; }
        try {
            User_24133057 user = activeUser(request, response); if (user == null) return;
            if (!SessionAction_24133057.valid(request)) { problem(request, response, 403, "Phiên thao tác hết hạn. Vui lòng tải lại trang."); return; }
            HttpSession session = request.getSession();
            synchronized (session) {
                Long start = (Long) session.getAttribute("activationWindow");
                if (start == null || System.currentTimeMillis() - start > 15 * 60 * 1000L) {
                    session.setAttribute("activationWindow", System.currentTimeMillis()); session.setAttribute("activationAttempts", 0);
                }
                int attempts = (Integer) session.getAttribute("activationAttempts");
                if (attempts >= 10) { problem(request, response, 429, "Bạn đã thử quá nhiều mã. Vui lòng thử lại sau 15 phút."); return; }
                session.setAttribute("activationAttempts", attempts + 1);
                String id = dao.redeem(user.getUsername(), request.getParameter("code"));
                session.setAttribute("activationAttempts", 0);
                flash(request, "flashSuccess", "Kích hoạt thành công. Video đã được thêm vào thư viện của bạn.");
                redirect(request, response, "/videos/detail?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8));
            }
        } catch (IllegalArgumentException e) { problem(request, response, 400, e.getMessage()); }
        catch (SQLException e) { problem(request, response, 503, "Chưa kích hoạt được video. Mã chưa bị sử dụng; vui lòng thử lại."); getServletContext().log("Activation failed", e); }
    }
    private void problem(HttpServletRequest request, HttpServletResponse response, int status, String message) throws ServletException, IOException {
        response.setStatus(status); render(request, response, "activate", Map.of("error", message, "videoCsrf", SessionAction_24133057.token(request)));
    }
}
