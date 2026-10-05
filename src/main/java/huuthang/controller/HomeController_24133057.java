package huuthang.controller;

import huuthang.config.DatabaseConnection_24133057;
import huuthang.config.ServiceFactory_24133057;
import huuthang.model.Page_24133057;
import huuthang.model.VideoView_24133057;
import huuthang.service.VideoService_24133057;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class HomeController_24133057 extends BaseController_24133057 {
    private static final long serialVersionUID = 1L;
    private transient VideoService_24133057 videoService;

    @Override
    public void init() {
        videoService = ServiceFactory_24133057.videoService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Page_24133057<VideoView_24133057> featured = videoService.getVideos(null, 1);
            Map<String, Object> model = new HashMap<>();
            model.put("featuredVideos", featured.getItems());
            model.put("categories", videoService.getCategories());
            model.put("databaseAvailable", DatabaseConnection_24133057.isAvailable());
            render(request, response, "home", model);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải trang chủ", exception);
        }
    }
}
