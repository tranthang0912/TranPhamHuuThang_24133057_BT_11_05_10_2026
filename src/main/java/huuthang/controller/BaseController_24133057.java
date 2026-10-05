package huuthang.controller;

import huuthang.model.User_24133057;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseController_24133057 extends HttpServlet {
    protected User_24133057 activeUser(HttpServletRequest request, HttpServletResponse response)
            throws java.sql.SQLException, IOException {
        HttpSession session = request.getSession(false);
        User_24133057 user = session == null ? null : (User_24133057) session.getAttribute("currentUser");
        if (user != null) {
            var fresh = huuthang.config.ServiceFactory_24133057.userService().findByUsername(user.getUsername());
            if (fresh.isPresent() && fresh.get().isActive()) {
                session.setAttribute("currentUser", fresh.get());
                return fresh.get();
            }
            session.invalidate();
        }
        if ("GET".equals(request.getMethod())) request.getSession().setAttribute("commerceReturnPath", request.getServletPath());
        flash(request, "flashError", "Vui lòng đăng nhập bằng tài khoản đang hoạt động để tiếp tục.");
        redirect(request, response, "/auth/login");
        return null;
    }
    protected void render(HttpServletRequest request, HttpServletResponse response,
                          String template, Map<String, Object> variables)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Map<String, Object> model = new HashMap<>();
        if (variables != null) {
            model.putAll(variables);
        }
        HttpSession session = request.getSession(false);
        User_24133057 currentUser = session == null ? null
                : (User_24133057) session.getAttribute("currentUser");
        model.put("contextPath", request.getContextPath());
        model.put("currentUser", currentUser);
        model.put("isAdmin", currentUser != null && currentUser.isAdmin());
        model.put("currentYear", Year.now().getValue());
        model.put("currentPath", request.getServletPath());
        copyFlash(session, model, "flashSuccess");
        copyFlash(session, model, "flashError");
        model.forEach(request::setAttribute);
        request.getRequestDispatcher("/WEB-INF/views/" + template + "_24133057.jsp")
                .forward(request, response);
    }

    protected void render(HttpServletRequest request, HttpServletResponse response, String template)
            throws ServletException, IOException {
        render(request, response, template, new HashMap<>());
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void flash(HttpServletRequest request, String key, String message) {
        request.getSession(true).setAttribute(key, message);
    }

    private void copyFlash(HttpSession session, Map<String, Object> model, String key) {
        if (session != null && session.getAttribute(key) != null) {
            model.put(key, session.getAttribute(key));
            session.removeAttribute(key);
        }
    }
}
