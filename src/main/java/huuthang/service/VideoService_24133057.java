package huuthang.service;

import huuthang.model.Category_24133057;
import huuthang.model.Page_24133057;
import huuthang.model.VideoView_24133057;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface VideoService_24133057 {
    List<Category_24133057> getCategories() throws SQLException;

    Optional<Category_24133057> getCategory(Integer categoryId) throws SQLException;

    Page_24133057<VideoView_24133057> getVideos(Integer categoryId, int page) throws SQLException;

    Optional<VideoView_24133057> getVideoDetailAndIncreaseView(String videoId) throws SQLException;

    Optional<VideoView_24133057> getVideoDetail(String videoId, boolean increaseView) throws SQLException;
}
