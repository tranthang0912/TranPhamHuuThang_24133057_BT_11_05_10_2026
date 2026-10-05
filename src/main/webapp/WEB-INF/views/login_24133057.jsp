<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>Đăng nhập | Hữu Thắng Video</title></head>
<body>
<main class="auth-page">
    <div class="auth-shell">
        <section class="auth-aside">
            <span class="eyebrow light">HỌC TẬP THEO CÁCH CỦA BẠN</span>
            <div class="auth-visual" aria-hidden="true"><span class="auth-play">▶</span><span class="auth-visual-line"></span><span class="auth-visual-line short"></span></div>
            <h1>Chào mừng<br>bạn trở lại.</h1>
            <p>Tiếp tục khám phá video học tập, lưu giỏ hàng và theo dõi các đơn đã đặt.</p>
            <ul class="feature-list"><li><span>✓</span> Video học tập theo chủ đề</li><li><span>✓</span> Giỏ hàng lưu theo tài khoản</li><li><span>✓</span> Theo dõi đơn hàng dễ dàng</li></ul>
            <a class="auth-aside-link" href="<c:url value='/shop'/>">Khám phá cửa hàng video <span>→</span></a>
        </section>
        <section class="auth-form-panel">
            <div class="form-heading"><span class="form-icon" aria-hidden="true">↗</span><h2>Đăng nhập</h2><p>Rất vui được gặp lại bạn. Nhập thông tin để tiếp tục.</p></div>
            <c:if test="${not empty flashSuccess}"><div class="alert success"><c:out value="${flashSuccess}"/></div></c:if>
            <c:if test="${not empty flashError}"><div class="alert error"><c:out value="${flashError}"/></div></c:if>
            <c:if test="${not empty error}"><div class="alert error" role="alert"><strong>Chưa thể đăng nhập</strong><span><c:out value="${error}"/></span></div></c:if>
            <form class="stack-form" method="post" action="<c:url value='/auth/login'/>">
                <label for="login-username">Tên đăng nhập<input id="login-username" type="text" name="username" maxlength="50" autocomplete="username" required autofocus value="<c:out value='${username}'/>" placeholder="Nhập tên đăng nhập"></label>
                <label for="login-password">Mật khẩu<span class="password-field"><input id="login-password" type="password" name="password" autocomplete="current-password" required placeholder="Nhập mật khẩu"><button type="button" data-password-toggle aria-controls="login-password" aria-label="Hiện mật khẩu" aria-pressed="false">Hiện</button></span></label>
                <button class="button primary wide" type="submit">Đăng nhập <span>→</span></button>
            </form>
            <p class="form-switch">Bạn chưa có tài khoản? <a href="<c:url value='/auth/register'/>">Tạo tài khoản miễn phí</a></p>
            <details class="demo-accounts"><summary>Tài khoản demo cho bài tập</summary><div><span><b>admin</b> / Admin@123</span><span><b>huuthang</b> / User@123</span></div></details>
        </section>
    </div>
</main>
</body>
</html>
