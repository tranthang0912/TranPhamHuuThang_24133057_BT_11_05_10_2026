package huuthang.dao.impl;

import huuthang.config.DatabaseConnection_24133057;
import huuthang.dao.VideoDao_24133057;
import huuthang.model.VideoView_24133057;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VideoDaoImpl_24133057 implements VideoDao_24133057 {
    private static final String SELECT_VIDEO =
            "SELECT v.VideoId, v.Title, v.Poster, COALESCE(v.Views, 0) AS Views, v.Description, "
                    + "v.Active, v.CategoryId, c.Categoryname, "
                    + "(SELECT COUNT(*) FROM dbo.Shares s WHERE s.VideoId = v.VideoId) AS ShareCount, "
                    + "(SELECT COUNT(*) FROM dbo.Favorites f WHERE f.VideoId = v.VideoId) AS LikeCount "
                    + "FROM dbo.Videos v LEFT JOIN dbo.Category c ON c.CategoryId = v.CategoryId ";

    @Override
    public Optional<VideoView_24133057> findById(String videoId) throws SQLException {
        String sql = SELECT_VIDEO + "WHERE v.VideoId = ? AND v.Active = 1";
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, videoId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapVideo(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public List<VideoView_24133057> findPageByCategory(Integer categoryId, int offset, int limit)
            throws SQLException {
        String where = categoryId == null
                ? "WHERE v.Active = 1 "
                : "WHERE v.Active = 1 AND v.CategoryId = ? ";
        String sql = SELECT_VIDEO + where
                + "ORDER BY v.VideoId OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<VideoView_24133057> videos = new ArrayList<>();
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            if (categoryId != null) {
                statement.setInt(index++, categoryId);
            }
            statement.setInt(index++, offset);
            statement.setInt(index, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    videos.add(mapVideo(resultSet));
                }
            }
        }
        return videos;
    }

    @Override
    public long countByCategory(Integer categoryId) throws SQLException {
        String sql = categoryId == null
                ? "SELECT COUNT_BIG(*) FROM dbo.Videos WHERE Active = 1"
                : "SELECT COUNT_BIG(*) FROM dbo.Videos WHERE Active = 1 AND CategoryId = ?";
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (categoryId != null) {
                statement.setInt(1, categoryId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    @Override
    public void incrementViews(String videoId) throws SQLException {
        String sql = "UPDATE dbo.Videos SET Views = COALESCE(Views, 0) + 1 WHERE VideoId = ? AND Active = 1";
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, videoId);
            statement.executeUpdate();
        }
    }

    private VideoView_24133057 mapVideo(ResultSet resultSet) throws SQLException {
        VideoView_24133057 video = new VideoView_24133057();
        video.setVideoId(resultSet.getString("VideoId"));
        video.setTitle(resultSet.getString("Title"));
        video.setPoster(resultSet.getString("Poster"));
        video.setViews(resultSet.getInt("Views"));
        video.setDescription(resultSet.getString("Description"));
        video.setActive(resultSet.getBoolean("Active"));
        int categoryId = resultSet.getInt("CategoryId");
        video.setCategoryId(resultSet.wasNull() ? null : categoryId);
        video.setCategoryName(resultSet.getString("Categoryname"));
        video.setShareCount(resultSet.getInt("ShareCount"));
        video.setLikeCount(resultSet.getInt("LikeCount"));
        return video;
    }
}
