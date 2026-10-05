package huuthang.controller;

import huuthang.config.ServiceFactory_24133057;
import huuthang.model.Page_24133057;
import huuthang.model.User_24133057;
import huuthang.service.UserService_24133057;
import huuthang.util.RequestUtil_24133057;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AdminUserController_24133057 extends BaseController_24133057 {
    private static final long serialVersionUID = 1L;
    private transient UserService_24133057 userService;

    @Override
    public void init() {
        userService = ServiceFactory_24133057.userService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("videoCsrf", huuthang.util.SessionAction_24133057.token(request));
        if (request.getServletPath().endsWith("/form")) {
            showForm(request, response);
        } else if (request.getServletPath().endsWith("/delete")) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Xóa user bắt buộc dùng POST");
        } else {
            showList(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!huuthang.util.SessionAction_24133057.valid(request)) {
            response.setStatus(403); render(request, response, "shop-error", Map.of("error", "Phiên thao tác hết hạn. Vui lòng tải lại trang.")); return;
        }
        if (request.getServletPath().endsWith("/delete")) {
            delete(request, response);
        } else if (request.getServletPath().endsWith("/form")) {
            save(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = RequestUtil_24133057.positiveInt(request.getParameter("page"), 1);
        try {
            Page_24133057<User_24133057> userPage = userService.getUsers(page);
            Map<String, Object> model = new HashMap<>();
            model.put("userPage", userPage);
            render(request, response, "admin-users", model);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải danh sách user", exception);
        }
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        try {
            boolean creating = username == null || username.isBlank();
            User_24133057 user;
            if (creating) {
                user = new User_24133057();
                user.setActive(true);
                user.setAdmin(false);
                user.setImages("/assets/images/avatar-user.svg");
            } else {
                Optional<User_24133057> found = userService.findByUsername(username);
                if (found.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy user");
                    return;
                }
                user = found.get();
            }
            renderForm(request, response, user, creating, null);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải user", exception);
        }
    }

    private void save(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean creating = Boolean.parseBoolean(request.getParameter("creating"));
        User_24133057 user = new User_24133057();
        user.setUsername(request.getParameter("username"));
        user.setFullname(request.getParameter("fullname"));
        user.setEmail(request.getParameter("email"));
        user.setPhone(request.getParameter("phone"));
        user.setImages(request.getParameter("images"));
        user.setAdmin(request.getParameter("admin") != null);
        user.setActive(request.getParameter("active") != null);
        String password = request.getParameter("password");
        try {
            userService.saveByAdmin(user, password, creating);
            flash(request, "flashSuccess", creating ? "Đã tạo user mới." : "Đã cập nhật user.");
            redirect(request, response, "/admin/users");
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderForm(request, response, user, creating, exception.getMessage());
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 2627 || exception.getErrorCode() == 2601) {
                response.setStatus(400); renderForm(request, response, user, creating, "Tên đăng nhập hoặc email đã được sử dụng.");
            } else throw new ServletException("Không thể lưu người dùng", exception);
        }
    }

    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        User_24133057 current = (User_24133057) request.getSession().getAttribute("currentUser");
        if (current != null && current.getUsername().equalsIgnoreCase(username)) {
            flash(request, "flashError", "Bạn không thể xóa chính tài khoản đang đăng nhập.");
            redirect(request, response, "/admin/users");
            return;
        }
        try {
            userService.delete(username);
            flash(request, "flashSuccess", "Đã xóa user " + username + ".");
            redirect(request, response, "/admin/users");
        } catch (SQLException exception) {
            if (exception.getErrorCode() == 547) {
                flash(request, "flashError", "Tài khoản đã có dữ liệu đơn hàng hoặc video kích hoạt. Hãy khóa tài khoản để giữ lại lịch sử.");
                redirect(request, response, "/admin/users");
            } else throw new ServletException("Không thể xóa người dùng", exception);
        }
    }

    private void renderForm(HttpServletRequest request, HttpServletResponse response,
                            User_24133057 user, boolean creating, String error)
            throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("formUser", user);
        model.put("videoCsrf", huuthang.util.SessionAction_24133057.token(request));
        model.put("creating", creating);
        model.put("error", error);
        render(request, response, "admin-user-form", model);
    }
}
