<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>Thông tin cá nhân | Hữu Thắng Video</title></head>
<body>
<c:url var="avatarUrl" value="${profileUser.images}"/>
<c:url var="avatarFallback" value="/assets/images/avatar-user.svg"/>
<main class="profile-page">
    <section class="container section-space">
        <a class="back-link" href="<c:url value='/home'/>">← Về trang chủ</a>
        <div class="profile-shell">
            <aside class="profile-identity">
                <span class="eyebrow light">TÀI KHOẢN CỦA BẠN</span>
                <img class="profile-avatar" src="${avatarUrl}" data-fallback="${avatarFallback}" alt="Ảnh đại diện của <c:out value='${profileUser.fullname}'/>" width="128" height="128">
                <h1><c:out value="${profileUser.fullname}"/></h1>
                <p>@<c:out value="${profileUser.username}"/></p>
                <span class="profile-role ${profileUser.admin ? 'admin' : ''}">${profileUser.admin ? 'Quản trị viên' : 'Người dùng'}</span>
            </aside>
            <section class="profile-details" aria-labelledby="profile-heading">
                <div class="profile-heading">
                    <div><span class="eyebrow">THÔNG TIN CÁ NHÂN</span><h2 id="profile-heading">Hồ sơ tài khoản</h2></div>
                    <span class="account-status"><i></i>${profileUser.active ? 'Đang hoạt động' : 'Chưa kích hoạt'}</span>
                </div>
                <dl class="profile-info-grid">
                    <div><dt>Tên đăng nhập</dt><dd><c:out value="${profileUser.username}"/></dd></div>
                    <div><dt>Họ và tên</dt><dd><c:out value="${profileUser.fullname}"/></dd></div>
                    <div><dt>Email</dt><dd><c:out value="${profileUser.email}"/></dd></div>
                    <div><dt>Số điện thoại</dt><dd><c:out value="${empty profileUser.phone ? 'Chưa cập nhật' : profileUser.phone}"/></dd></div>
                    <div><dt>Vai trò</dt><dd>${profileUser.admin ? 'Quản trị viên' : 'Người dùng'}</dd></div>
                    <div><dt>Trạng thái</dt><dd>${profileUser.active ? 'Đã kích hoạt' : 'Chưa kích hoạt'}</dd></div>
                </dl>
                <div class="profile-actions">
                    <a class="button primary" href="<c:url value='/account/videos'/>">Video của tôi</a>
                    <a class="button ghost" href="<c:url value='/account/activate'/>">Kích hoạt video</a>
                    <c:if test="${profileUser.admin}"><a class="button ghost" href="<c:url value='/admin'/>">Trang quản trị</a></c:if>
                    <a class="button logout-button" href="<c:url value='/auth/logout'/>">Đăng xuất</a>
                </div>
                <p class="profile-security-note">Mật khẩu được bảo vệ và không hiển thị trên trang hồ sơ.</p>
            </section>
        </div>
    </section>
</main>
</body>
</html>
