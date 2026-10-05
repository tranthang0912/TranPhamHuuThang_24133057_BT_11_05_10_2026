package huuthang.controller;

import huuthang.config.ServiceFactory_24133057;
import huuthang.model.Category_24133057;
import huuthang.model.Page_24133057;
import huuthang.model.User_24133057;
import huuthang.model.VideoView_24133057;
import huuthang.service.UserService_24133057;
import huuthang.service.VideoService_24133057;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminHomeController_24133057 extends BaseController_24133057 {
    private static final long serialVersionUID = 1L;
    private transient UserService_24133057 userService;
    private transient VideoService_24133057 videoService;

    @Override
    public void init() {
        userService = ServiceFactory_24133057.userService();
        videoService = ServiceFactory_24133057.videoService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Page_24133057<User_24133057> users = userService.getUsers(1);
            Page_24133057<VideoView_24133057> videos = videoService.getVideos(null, 1);
            List<Category_24133057> categories = videoService.getCategories();
            long totalVideos = categories.stream().mapToLong(Category_24133057::getVideoCount).sum();
            Map<String, Object> model = new HashMap<>();
            model.put("totalUsers", users.getTotalItems());
            model.put("totalVideos", totalVideos);
            model.put("totalCategories", categories.size());
            model.put("recentVideos", videos.getItems());
            render(request, response, "admin-dashboard", model);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải trang quản trị", exception);
        }
    }
}
