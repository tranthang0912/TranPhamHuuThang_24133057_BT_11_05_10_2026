package huuthang.dao;

import huuthang.model.User_24133057;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDao_24133057 {
    Optional<User_24133057> findByUsername(String username) throws SQLException;

    Optional<User_24133057> findByEmail(String email) throws SQLException;

    List<User_24133057> findPage(int offset, int limit) throws SQLException;

    long count() throws SQLException;

    void create(User_24133057 user) throws SQLException;

    void update(User_24133057 user) throws SQLException;

    void activate(String username) throws SQLException;

    void delete(String username) throws SQLException;
}
