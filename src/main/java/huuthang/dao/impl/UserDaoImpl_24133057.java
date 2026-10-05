package huuthang.dao.impl;

import huuthang.config.DatabaseConnection_24133057;
import huuthang.dao.UserDao_24133057;
import huuthang.model.User_24133057;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDaoImpl_24133057 implements UserDao_24133057 {
    private static final String SELECT_COLUMNS =
            "SELECT Username, Password, Phone, Fullname, Email, Admin, Active, Images FROM dbo.Users ";

    @Override
    public Optional<User_24133057> findByUsername(String username) throws SQLException {
        String sql = SELECT_COLUMNS + "WHERE Username = ?";
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapUser(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<User_24133057> findByEmail(String email) throws SQLException {
        String sql = SELECT_COLUMNS + "WHERE Email = ?";
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapUser(resultSet)) : Optional.empty();
            }
        }
    }

    @Override
    public List<User_24133057> findPage(int offset, int limit) throws SQLException {
        String sql = SELECT_COLUMNS + "ORDER BY Username OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<User_24133057> users = new ArrayList<>();
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, offset);
            statement.setInt(2, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
            }
        }
        return users;
    }

    @Override
    public long count() throws SQLException {
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT_BIG(*) FROM dbo.Users");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    @Override
    public void create(User_24133057 user) throws SQLException {
        String sql = "INSERT dbo.Users (Username, Password, Phone, Fullname, Email, Admin, Active, Images) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindUser(statement, user, false);
            statement.executeUpdate();
        }
    }

    @Override
    public void update(User_24133057 user) throws SQLException {
        String sql = "UPDATE dbo.Users SET Password = ?, Phone = ?, Fullname = ?, Email = ?, "
                + "Admin = ?, Active = ?, Images = ? WHERE Username = ?";
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getPassword());
            statement.setString(2, user.getPhone());
            statement.setString(3, user.getFullname());
            statement.setString(4, user.getEmail());
            statement.setBoolean(5, user.isAdmin());
            statement.setBoolean(6, user.isActive());
            statement.setString(7, user.getImages());
            statement.setString(8, user.getUsername());
            statement.executeUpdate();
        }
    }

    @Override
    public void activate(String username) throws SQLException {
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE dbo.Users SET Active = 1 WHERE Username = ?")) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(String username) throws SQLException {
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE dbo.Users WHERE Username = ?")) {
            statement.setString(1, username);
            statement.executeUpdate();
        }
    }

    private void bindUser(PreparedStatement statement, User_24133057 user, boolean update) throws SQLException {
        int index = 1;
        if (!update) {
            statement.setString(index++, user.getUsername());
        }
        statement.setString(index++, user.getPassword());
        statement.setString(index++, user.getPhone());
        statement.setString(index++, user.getFullname());
        statement.setString(index++, user.getEmail());
        statement.setBoolean(index++, user.isAdmin());
        statement.setBoolean(index++, user.isActive());
        statement.setString(index, user.getImages());
    }

    private User_24133057 mapUser(ResultSet resultSet) throws SQLException {
        User_24133057 user = new User_24133057();
        user.setUsername(resultSet.getString("Username"));
        user.setPassword(resultSet.getString("Password"));
        user.setPhone(resultSet.getString("Phone"));
        user.setFullname(resultSet.getString("Fullname"));
        user.setEmail(resultSet.getString("Email"));
        user.setAdmin(nullableBoolean(resultSet, "Admin"));
        user.setActive(nullableBoolean(resultSet, "Active"));
        user.setImages(resultSet.getString("Images"));
        return user;
    }

    private Boolean nullableBoolean(ResultSet resultSet, String column) throws SQLException {
        boolean value = resultSet.getBoolean(column);
        return resultSet.wasNull() ? null : value;
    }
}
