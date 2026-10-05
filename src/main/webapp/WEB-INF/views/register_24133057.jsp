<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>Đăng ký | Hữu Thắng Video</title></head>
<body>
<main class="auth-page register-page">
    <div class="auth-shell">
        <section class="auth-aside register-aside">
            <span class="eyebrow light">BẮT ĐẦU HÀNH TRÌNH HỌC TẬP</span>
            <div class="auth-visual" aria-hidden="true"><span class="auth-play">▶</span><span class="auth-visual-line"></span><span class="auth-visual-line short"></span></div>
            <h1>Mỗi ngày,<br>học thêm một điều.</h1>
            <p>Một tài khoản để khám phá video, mua nội dung học tập và quản lý đơn hàng của bạn.</p>
            <ul class="feature-list"><li><span>✓</span> Kích hoạt tài khoản bằng OTP</li><li><span>✓</span> Video theo chủ đề bạn quan tâm</li><li><span>✓</span> Mua hàng và theo dõi đơn thuận tiện</li></ul>
            <p class="auth-aside-note">Miễn phí tạo tài khoản • Xác thực qua email</p>
        </section>
        <section class="auth-form-panel">
            <div class="form-heading"><span class="form-icon" aria-hidden="true">＋</span><h2>Tạo tài khoản</h2><p>Điền thông tin bên dưới. Chúng tôi sẽ gửi OTP tới email của bạn.</p></div>
            <c:if test="${not empty flashSuccess}"><div class="alert success" role="status"><c:out value="${flashSuccess}"/></div></c:if>
            <c:if test="${not empty flashError}"><div class="alert error" role="alert"><c:out value="${flashError}"/></div></c:if>
            <c:if test="${not empty error}"><div class="alert error" role="alert"><strong>Kiểm tra lại thông tin</strong><span><c:out value="${error}"/></span></div></c:if>
            <form class="stack-form compact" method="post" action="<c:url value='/auth/register'/>" data-confirm-password>
                <div class="field-row">
                    <label for="register-username">Tên đăng nhập *<input id="register-username" name="username" minlength="3" maxlength="50" pattern="[A-Za-z0-9._\-]{3,50}" autocomplete="username" required value="<c:out value='${formUser.username}'/>" placeholder="Ví dụ: nguyenan"><small class="input-hint">3–50 ký tự: chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.</small></label>
                    <label for="register-fullname">Họ và tên *<input id="register-fullname" name="fullname" minlength="2" maxlength="50" autocomplete="name" required value="<c:out value='${formUser.fullname}'/>" placeholder="Nguyễn Văn An"></label>
                </div>
                <label for="register-email">Email *<input id="register-email" type="email" name="email" maxlength="150" autocomplete="email" required value="<c:out value='${formUser.email}'/>" placeholder="ban@example.com"><small class="input-hint">Dùng email có thể nhận thư để lấy mã OTP.</small></label>
                <label for="register-phone">Số điện thoại <span class="optional-label">(không bắt buộc)</span><input id="register-phone" type="tel" name="phone" maxlength="12" pattern="(0|\+84)[35789][0-9]{8}" autocomplete="tel" value="<c:out value='${formUser.phone}'/>" placeholder="0912345678"></label>
                <div class="field-row">
                    <label for="register-password">Mật khẩu *<span class="password-field"><input id="register-password" type="password" name="password" minlength="6" maxlength="128" autocomplete="new-password" required placeholder="Tối thiểu 6 ký tự"><button type="button" data-password-toggle aria-controls="register-password" aria-label="Hiện mật khẩu" aria-pressed="false">Hiện</button></span></label>
                    <label for="register-confirm">Xác nhận mật khẩu *<span class="password-field"><input id="register-confirm" type="password" name="confirmPassword" minlength="6" maxlength="128" autocomplete="new-password" required placeholder="Nhập lại mật khẩu"><button type="button" data-password-toggle aria-controls="register-confirm" aria-label="Hiện mật khẩu xác nhận" aria-pressed="false">Hiện</button></span></label>
                </div>
                <button class="button primary wide" type="submit">Tạo tài khoản &amp; nhận OTP <span aria-hidden="true">→</span></button>
                <p class="form-note">Mã OTP có hiệu lực trong 5 phút sau khi gửi.</p>
            </form>
            <p class="form-switch">Đã có tài khoản? <a href="<c:url value='/auth/login'/>">Đăng nhập</a></p>
        </section>
    </div>
</main>
</body>
</html>
