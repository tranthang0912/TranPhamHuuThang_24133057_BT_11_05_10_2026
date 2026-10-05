package huuthang.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection_24133057 {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = DatabaseConnection_24133057.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IllegalStateException("Không tìm thấy application.properties");
            }
            PROPERTIES.load(input);
            Class.forName(PROPERTIES.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private DatabaseConnection_24133057() {
    }

    public static Connection getConnection() throws SQLException {
        String url = environmentOrDefault("DB_URL", PROPERTIES.getProperty("db.url"));
        String username = environmentOrDefault("DB_USERNAME", PROPERTIES.getProperty("db.username"));
        String password = environmentOrDefault("DB_PASSWORD", PROPERTIES.getProperty("db.password"));
        return DriverManager.getConnection(url, username, password);
    }

    public static boolean isAvailable() {
        try (Connection ignored = getConnection()) {
            return true;
        } catch (SQLException exception) {
            return false;
        }
    }

    private static String environmentOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
