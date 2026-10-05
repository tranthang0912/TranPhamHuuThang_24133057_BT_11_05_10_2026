package huuthang.model;

import huuthang.config.DatabaseConnection_24133057;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseIntegrationTest_24133057 {
    @Test
    void databaseContainsRequiredTablesAndSeedData() throws Exception {
        try (Connection connection = DatabaseConnection_24133057.getConnection()) {
            assertEquals("VideoManagement_24133057", connection.getCatalog());
            assertEquals(5, scalar(connection,
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE' "
                            + "AND TABLE_NAME IN ('Users','Category','Videos','Shares','Favorites')"));
            assertTrue(scalar(connection, "SELECT COUNT(*) FROM dbo.Videos") >= 10);
            assertTrue(scalar(connection, "SELECT COUNT(*) FROM dbo.Category") >= 3);
            assertTrue(scalar(connection, "SELECT COUNT(*) FROM dbo.Users") >= 2);
        }
    }

    @Test
    void videoIdIsNvarchar50AndAutomaticallyGenerated() throws Exception {
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE, COLUMN_DEFAULT "
                             + "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='Videos' AND COLUMN_NAME='VideoId'");
             ResultSet resultSet = statement.executeQuery()) {
            assertTrue(resultSet.next());
            assertEquals("nvarchar", resultSet.getString("DATA_TYPE"));
            assertEquals(50, resultSet.getInt("CHARACTER_MAXIMUM_LENGTH"));
            assertEquals("NO", resultSet.getString("IS_NULLABLE"));
            assertTrue(resultSet.getString("COLUMN_DEFAULT").contains("VideoIdSequence"));
        }
    }

    @Test
    void seededVietnameseVideoTextMatchesUtf8Script() throws Exception {
        String script = Files.readString(Path.of("database", "schema-and-data.sql"),
                StandardCharsets.UTF_8);
        int repairStart = script.indexOf("UPDATE dbo.Users SET Fullname");
        int repairEnd = script.indexOf("\nGO", repairStart);
        String repairSql = script.substring(repairStart, repairEnd);
        Map<String, String> titles = new LinkedHashMap<>();
        Map<String, String> descriptions = new LinkedHashMap<>();
        Matcher matcher = Pattern.compile("WHEN N'(VID[0-9]+)' THEN N'((?:[^']|'')*)'")
                .matcher(repairSql);
        while (matcher.find()) {
            String videoId = matcher.group(1);
            String value = matcher.group(2).replace("''", "'");
            if (titles.containsKey(videoId)) {
                descriptions.put(videoId, value);
            } else {
                titles.put(videoId, value);
            }
        }

        int verified = 0;
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT VideoId, Title, Description FROM dbo.Videos ORDER BY VideoId");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String videoId = resultSet.getString("VideoId");
                if (!titles.containsKey(videoId)) {
                    continue;
                }
                assertEquals(titles.get(videoId), resultSet.getNString("Title"), videoId + " title");
                assertEquals(descriptions.get(videoId), resultSet.getNString("Description"),
                        videoId + " description");
                verified++;
            }
        }
        assertEquals(10, verified);
    }

    @Test
    void seededVideosUseValidYoutubeThumbnailsWithinExamColumnLength() throws Exception {
        try (Connection connection = DatabaseConnection_24133057.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT Poster FROM dbo.Videos WHERE VideoId BETWEEN N'VID00000001' AND N'VID00000010'");
             ResultSet resultSet = statement.executeQuery()) {
            int verified = 0;
            while (resultSet.next()) {
                String poster = resultSet.getNString("Poster");
                assertTrue(poster.matches("https://i\\.ytimg\\.com/vi/[A-Za-z0-9_-]{11}/hqdefault\\.jpg"));
                assertTrue(poster.length() <= 50);
                verified++;
            }
            assertEquals(10, verified);
        }
    }

    private int scalar(Connection connection, String sql) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }
}
