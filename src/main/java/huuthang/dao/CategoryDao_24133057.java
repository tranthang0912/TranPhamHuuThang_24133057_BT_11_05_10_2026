package huuthang.dao;

import huuthang.model.Category_24133057;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CategoryDao_24133057 {
    List<Category_24133057> findAllWithVideoCount() throws SQLException;

    Optional<Category_24133057> findById(int categoryId) throws SQLException;
}
