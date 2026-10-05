<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Kiểm tra JSP | 24133057</title>
</head>
<body>
<main class="container section-space">
    <section class="panel status-panel">
        <span class="eyebrow">JSP COMPATIBILITY</span>
        <h1>Servlet + JDBC + JSP hoạt động</h1>
        <p>Trang này được forward từ <code>JspCompatibilityController_24133057</code> để đáp ứng nền tảng JSP của đề.</p>
        <p>SQL Server: <strong>${databaseAvailable ? 'Đã kết nối' : 'Chưa kết nối'}</strong></p>
        <a class="button primary" href="${pageContext.request.contextPath}/home">Về trang chủ</a>
    </section>
</main>
</body>
</html>
