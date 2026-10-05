package huuthang.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JspViewTest_24133057 {
    private static final Path VIEW_DIRECTORY = Path.of("src", "main", "webapp", "WEB-INF", "views");
    private static final List<String> REQUIRED_VIEWS = List.of(
            "home", "login", "register", "verify-otp", "videos", "video-detail",
            "profile", "admin-dashboard", "admin-users", "admin-user-form", "error",
            "shop", "cart", "checkout", "orders", "order-detail", "shop-error", "activate", "my-videos", "favorites"
    );

    @Test
    void allBusinessViewsUseUtf8JspAndJstl() throws IOException {
        for (String view : REQUIRED_VIEWS) {
            Path file = VIEW_DIRECTORY.resolve(view + "_24133057.jsp");
            assertTrue(Files.isRegularFile(file), () -> "Thiếu JSP: " + file);
            String source = Files.readString(file, StandardCharsets.UTF_8);
            assertTrue(source.contains("pageEncoding=\"UTF-8\""), () -> "JSP chưa khai báo UTF-8: " + file);
            assertTrue(source.contains("<html lang=\"vi\">"), () -> "JSP thiếu tài liệu HTML tiếng Việt: " + file);
            assertFalse(source.contains("th:"), () -> "Còn thuộc tính template engine cũ: " + file);
        }
    }

    @Test
    void sitemeshDefinesSeparateUserAndAdminDecorators() throws IOException {
        String config = Files.readString(
                Path.of("src", "main", "webapp", "WEB-INF", "sitemesh3.xml"),
                StandardCharsets.UTF_8
        );
        assertTrue(config.contains("decorator=\"user_24133057.jsp\""));
        assertTrue(config.contains("decorator=\"admin_24133057.jsp\""));
        assertTrue(config.contains("/admin/*"));

        Path decoratorDirectory = Path.of("src", "main", "webapp", "WEB-INF", "decorators");
        for (String decorator : List.of("user_24133057.jsp", "admin_24133057.jsp")) {
            String source = Files.readString(decoratorDirectory.resolve(decorator), StandardCharsets.UTF_8);
            assertTrue(source.contains("${pageContext.request.contextPath}/assets/css/app.css"));
            assertFalse(source.matches("(?s).*(?:href|src)=\"/(?!/).*"),
                    () -> "Decorator còn URL tuyệt đối gây 404: " + decorator);
        }
    }

    @Test
    void authenticatedNavigationUsesSessionAndProvidesProfile() throws IOException {
        Path decoratorDirectory = Path.of("src", "main", "webapp", "WEB-INF", "decorators");
        String userDecorator = Files.readString(
                decoratorDirectory.resolve("user_24133057.jsp"), StandardCharsets.UTF_8);
        String adminDecorator = Files.readString(
                decoratorDirectory.resolve("admin_24133057.jsp"), StandardCharsets.UTF_8);
        String webXml = Files.readString(
                Path.of("src", "main", "webapp", "WEB-INF", "web.xml"), StandardCharsets.UTF_8);

        assertTrue(userDecorator.contains("not empty sessionScope.currentUser"));
        assertTrue(userDecorator.contains("/account/profile"));
        assertTrue(userDecorator.contains("/auth/logout"));
        assertTrue(adminDecorator.contains("/account/profile"));
        assertFalse(adminDecorator.contains("href=\"${pageContext.request.contextPath}/auth/login\""));
        assertTrue(webXml.contains("huuthang.controller.ProfileController_24133057"));
        assertTrue(webXml.contains("<url-pattern>/account/profile</url-pattern>"));
    }

    @Test
    void videoDetailEmbedsRealYoutubeVideoWithoutChangingSchema() throws IOException {
        String detail = Files.readString(
                VIEW_DIRECTORY.resolve("video-detail_24133057.jsp"), StandardCharsets.UTF_8);
        String script = Files.readString(Path.of("database", "schema-and-data.sql"),
                StandardCharsets.UTF_8);

        assertTrue(detail.contains("youtube-nocookie.com/embed/${video.youtubeId}"));
        assertTrue(script.contains("https://i.ytimg.com/vi/PkZNo7MFNFg/hqdefault.jpg"));
        assertTrue(script.contains("Poster NVARCHAR(50) NULL"));
    }
}
