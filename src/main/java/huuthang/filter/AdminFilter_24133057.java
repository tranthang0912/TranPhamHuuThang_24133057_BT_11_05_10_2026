package huuthang.filter;

import huuthang.model.User_24133057;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AdminFilter_24133057 implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        User_24133057 user = session == null ? null : (User_24133057) session.getAttribute("currentUser");
        try {
            if (user != null) {
                var refreshed = huuthang.config.ServiceFactory_24133057.userService().findByUsername(user.getUsername());
                if (refreshed.isEmpty() || !refreshed.get().isActive()) { session.invalidate(); session = null; user = null; }
                else { user = refreshed.get(); session.setAttribute("currentUser", user); }
            }
        } catch (java.sql.SQLException e) { httpResponse.sendError(503, "Chưa xác minh được quyền truy cập. Vui lòng thử lại."); return; }
        if (user == null || !user.isAdmin()) {
            httpRequest.getSession().setAttribute("flashError", "Bạn cần tài khoản quản trị để truy cập trang này.");
            httpResponse.sendRedirect(httpRequest.getContextPath() + (user == null ? "/auth/login" : "/home"));
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
