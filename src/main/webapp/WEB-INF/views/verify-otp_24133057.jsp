<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>Xác thực OTP | Hữu Thắng Video</title></head>
<body>
<main class="center-page">
    <section class="otp-card">
        <div class="otp-icon">✉</div><span class="eyebrow">XÁC THỰC EMAIL</span><h1>Kiểm tra hộp thư của bạn</h1>
        <p>Mã gồm 6 chữ số đã được gửi đến <strong><c:out value="${pendingEmail}"/></strong>. Mã hết hạn sau 5 phút.</p>
        <c:if test="${not empty flashSuccess}"><div class="alert success"><c:out value="${flashSuccess}"/></div></c:if>
        <c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>
        <form method="post" action="<c:url value='/auth/verify-otp'/>">
            <label class="visually-hidden" for="registration-otp">Mã OTP gồm 6 chữ số</label>
            <input id="registration-otp" class="otp-input" name="otp" inputmode="numeric" pattern="[0-9]{6}" maxlength="6" autocomplete="one-time-code" required autofocus placeholder="000000">
            <button class="button primary wide" type="submit">Xác nhận tài khoản</button>
        </form>
        <a class="text-link" href="<c:url value='/auth/register'/>">← Đăng ký lại</a>
    </section>
</main>
</body>
</html>
