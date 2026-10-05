package huuthang.controller;

import huuthang.config.ServiceFactory_24133057;
import huuthang.model.Category_24133057;
import huuthang.model.Page_24133057;
import huuthang.model.VideoView_24133057;
import huuthang.service.VideoService_24133057;
import huuthang.util.RequestUtil_24133057;
import huuthang.util.SessionAction_24133057;
import huuthang.dao.ActivationDao_24133057;
import huuthang.dao.VideoInteractionDao_24133057;
import huuthang.model.User_24133057;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class VideoController_24133057 extends BaseController_24133057 {
    private static final long serialVersionUID = 1L;
    private transient VideoService_24133057 videoService;
    private final VideoInteractionDao_24133057 interactions = new VideoInteractionDao_24133057();
    private final ActivationDao_24133057 activation = new ActivationDao_24133057();

    @Override
    public void init() {
        videoService = ServiceFactory_24133057.videoService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        if (request.getServletPath().endsWith("/favorites")) {
            try {
                User_24133057 user = activeUser(request, response);
                if (user != null) render(request, response, "favorites", Map.of("videos", interactions.favorites(user.getUsername())));
            } catch (SQLException e) { throw new ServletException("Không thể tải video yêu thích", e); }
        } else if (request.getServletPath().endsWith("/favorite") || request.getServletPath().endsWith("/share")) {
            response.sendError(405);
        } else if (request.getServletPath().endsWith("/detail")) {
            showDetail(request, response);
        } else {
            showList(request, response);
        }
    }

    private void showList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer categoryId = null;
        if (request.getParameter("categoryId") != null && !request.getParameter("categoryId").isBlank()) {
            categoryId = RequestUtil_24133057.positiveInt(request.getParameter("categoryId"), 0);
            if (categoryId == 0) {
                categoryId = null;
            }
        }
        int page = RequestUtil_24133057.positiveInt(request.getParameter("page"), 1);
        try {
            Page_24133057<VideoView_24133057> videoPage = videoService.getVideos(categoryId, page);
            Optional<Category_24133057> selected = videoService.getCategory(categoryId);
            Map<String, Object> model = new HashMap<>();
            model.put("categories", videoService.getCategories());
            model.put("selectedCategory", selected.orElse(null));
            model.put("selectedCategoryId", categoryId);
            model.put("videoPage", videoPage);
            render(request, response, "videos", model);
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải danh sách video", exception);
        }
    }

    private void showDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String id = validId(request.getParameter("id"));
            Optional<VideoView_24133057> video = videoService.getVideoDetail(id, false);
            if (video.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy video");
                return;
            }
            Map<String, Object> model = new HashMap<>();
            var session = request.getSession();
            User_24133057 user = (User_24133057) session.getAttribute("currentUser");
            if (user != null) {
                var fresh = ServiceFactory_24133057.userService().findByUsername(user.getUsername());
                if (fresh.isEmpty() || !fresh.get().isActive()) { session.removeAttribute("currentUser"); user = null; }
                else { user = fresh.get(); session.setAttribute("currentUser", user); }
            }
            boolean paid = activation.requiresActivation(id);
            boolean canWatch = !paid || (user != null && activation.unlocked(user.getUsername(), id));
            if (canWatch) synchronized (session) {
                @SuppressWarnings("unchecked") Map<String, Long> viewed = (Map<String, Long>) session.getAttribute("videoViews");
                if (viewed == null) { viewed = new LinkedHashMap<>(); session.setAttribute("videoViews", viewed); }
                long now = System.currentTimeMillis();
                if (now - viewed.getOrDefault(id, 0L) >= 30 * 60 * 1000L) {
                    video = videoService.getVideoDetail(id, true);
                    if (video.isEmpty()) { response.sendError(404, "Video đã ngừng hoạt động."); return; }
                    if (viewed.size() >= 100) viewed.remove(viewed.keySet().iterator().next());
                    viewed.put(id, now);
                }
            }
            model.put("canWatch", canWatch);
            model.put("liked", user != null && interactions.liked(user.getUsername(), id));
            model.put("videoCsrf", SessionAction_24133057.token(request));
            model.put("video", video.get());
            render(request, response, "video-detail", model);
        } catch (IllegalArgumentException e) {
            response.sendError(404, "Không tìm thấy video");
        } catch (SQLException exception) {
            throw new ServletException("Không thể tải chi tiết video", exception);
        }
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        boolean share = "/videos/share".equals(request.getServletPath());
        if (!share && !"/videos/favorite".equals(request.getServletPath())) { response.sendError(405); return; }
        try {
            User_24133057 user;
            if (share) {
                var session = request.getSession(false);
                user = session == null ? null : (User_24133057) session.getAttribute("currentUser");
                if (user == null || ServiceFactory_24133057.userService().findByUsername(user.getUsername()).filter(User_24133057::isActive).isEmpty()) {
                    json(response, 401, "{\"error\":\"Vui lòng đăng nhập để ghi nhận lượt chia sẻ.\"}"); return;
                }
            } else { user = activeUser(request, response); if (user == null) return; }
            if (!SessionAction_24133057.valid(request)) {
                if (share) json(response, 403, "{\"error\":\"Phiên thao tác hết hạn. Vui lòng tải lại trang.\"}");
                else { response.setStatus(403); render(request, response, "shop-error", Map.of("error", "Phiên thao tác không hợp lệ. Vui lòng tải lại trang.")); }
                return;
            }
            String id = validId(request.getParameter("id"));
            if (share) {
                interactions.share(user.getUsername(), id);
                long count = videoService.getVideoDetail(id, false).orElseThrow(() -> new IllegalArgumentException("Video đã ngừng hoạt động.")).getShareCount();
                json(response, 200, "{\"shareCount\":" + count + "}");
            } else {
                String desired = request.getParameter("desired");
                if (!"1".equals(desired) && !"0".equals(desired)) throw new IllegalArgumentException("Thao tác yêu thích không hợp lệ.");
                interactions.favorite(user.getUsername(), id, "1".equals(desired));
                flash(request, "flashSuccess", "1".equals(desired) ? "Đã thêm video vào danh sách yêu thích." : "Đã bỏ yêu thích video.");
                redirect(request, response, "/videos/detail?id=" + URLEncoder.encode(id, StandardCharsets.UTF_8));
            }
        } catch (IllegalArgumentException e) {
            if (share) json(response, 400, "{\"error\":\"Video hoặc thao tác không hợp lệ.\"}");
            else { response.setStatus(400); render(request, response, "shop-error", Map.of("error", e.getMessage())); }
        } catch (SQLException e) {
            getServletContext().log("Video interaction failed", e);
            if (share) json(response, 503, "{\"error\":\"Chưa ghi nhận được lượt chia sẻ. Vui lòng thử lại.\"}");
            else { response.setStatus(503); render(request, response, "shop-error", Map.of("error", "Chưa cập nhật được yêu thích. Vui lòng thử lại.")); }
        }
    }
    private String validId(String id) {
        if (id == null || id.isBlank() || id.length() > 50 || !id.matches("[A-Za-z0-9_-]+")) throw new IllegalArgumentException("Mã video không hợp lệ.");
        return id;
    }
    private void json(HttpServletResponse response, int status, String content) throws IOException {
        response.setStatus(status); response.setContentType("application/json;charset=UTF-8"); response.getWriter().write(content);
    }
}
