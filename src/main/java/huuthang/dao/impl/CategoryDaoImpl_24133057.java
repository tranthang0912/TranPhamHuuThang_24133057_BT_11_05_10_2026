package huuthang.dao.impl;

import huuthang.config.DatabaseConnection_24133057;
import huuthang.dao.CategoryDao_24133057;
import huuthang.model.Category_24133057;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryDaoImpl_24133057 implements CategoryDao_24133057 {
    @Override
    public List<Category_24133057> findAllWithVideoCount() throws SQLException {
        String sql = "SELECT c.CategoryId, c.Categoryname, c.Categorycode, c.Images, c.Status, "
                + "COUNT(v.VideoId) AS VideoCount FROM dbo.Category c "
                + "LEFT JOIN dbo.Videos v ON v.CategoryId = c.CategoryId AND v.Active = 1 "
                + "WHERE c.Status = 1 GROUP BY c.CategoryId, c.Categoryname, c.Categorycode, c.Images, c.Status "
                + "ORDER BY c.Categoryname";
        List<Category_24133057> categories = new ArrayList<>();
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(mapCategory(resultSet));
            }
        }
        return categories;
    }

    @Override
    public Optional<Category_24133057> findById(int categoryId) throws SQLException {
        String sql = "SELECT c.CategoryId, c.Categoryname, c.Categorycode, c.Images, c.Status, "
                + "COUNT(v.VideoId) AS VideoCount FROM dbo.Category c "
                + "LEFT JOIN dbo.Videos v ON v.CategoryId = c.CategoryId AND v.Active = 1 "
                + "WHERE c.CategoryId = ? "
                + "GROUP BY c.CategoryId, c.Categoryname, c.Categorycode, c.Images, c.Status";
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapCategory(resultSet)) : Optional.empty();
            }
        }
    }

    private Category_24133057 mapCategory(ResultSet resultSet) throws SQLException {
        Category_24133057 category = new Category_24133057();
        category.setCategoryId(resultSet.getInt("CategoryId"));
        category.setCategoryName(resultSet.getString("Categoryname"));
        category.setCategoryCode(resultSet.getString("Categorycode"));
        category.setImages(resultSet.getString("Images"));
        category.setStatus(resultSet.getBoolean("Status"));
        category.setVideoCount(resultSet.getInt("VideoCount"));
        return category;
    }
}
