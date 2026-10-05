package huuthang.service;

import huuthang.model.Page_24133057;
import huuthang.model.User_24133057;

import java.sql.SQLException;
import java.util.Optional;

public interface UserService_24133057 {
    Optional<User_24133057> authenticate(String username, String password) throws SQLException;

    void register(User_24133057 user, String rawPassword) throws SQLException;

    void activate(String username) throws SQLException;

    Page_24133057<User_24133057> getUsers(int page) throws SQLException;

    Optional<User_24133057> findByUsername(String username) throws SQLException;

    void saveByAdmin(User_24133057 user, String rawPassword, boolean creating) throws SQLException;

    void delete(String username) throws SQLException;
}
