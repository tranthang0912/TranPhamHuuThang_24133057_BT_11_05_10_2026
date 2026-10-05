package huuthang.dao;

import huuthang.model.VideoView_24133057;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface VideoDao_24133057 {
    Optional<VideoView_24133057> findById(String videoId) throws SQLException;

    List<VideoView_24133057> findPageByCategory(Integer categoryId, int offset, int limit) throws SQLException;

    long countByCategory(Integer categoryId) throws SQLException;

    void incrementViews(String videoId) throws SQLException;
}
