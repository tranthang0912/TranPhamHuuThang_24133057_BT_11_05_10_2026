package huuthang.controller;

import huuthang.config.ServiceFactory_24133057;
import huuthang.model.User_24133057;
import huuthang.service.UserService_24133057;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProfileController_24133057 extends BaseController_24133057 {
    private static final long serialVersionUID = 1L;
    private transient UserService_24133057 userService;

    @Override
    public void init() {
        userService = ServiceFactory_24133057.userService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User_24133057 signedInUser = session == null ? null
                : (User_24133057) session.getAttribute("currentUser");
        if (signedInUser == null) {
            flash(request, "flashError", "Vui lòng đăng nhập để xem thông tin cá nhân.");
            redirect(request, response, "/auth/login");
            return;
        }

        try {
            Optional<User_24133057> refreshed = userService.findByUsername(signedInUser.getUsername());
            if (refreshed.isEmpty() || !refreshed.get().isActive()) {
                session.invalidate();
                redirect(request, response, "/auth/login");
                return;
            }
            session.setAttribute("currentUser", refreshed.get());
            Map<String, Object> model = new HashMap<>();
            model.put("profileUser", refreshed.get());
            render(request, response, "profile", model);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải thông tin cá nhân", exception);
        }
    }
}
