<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>${creating ? 'Thêm người dùng' : 'Cập nhật người dùng'} | 24133057</title></head>
<body>
<main class="container section-space narrow-container">
    <a class="back-link" href="<c:url value='/admin/users'/>">← Danh sách người dùng</a>
    <section class="form-panel">
        <div class="section-heading"><span class="eyebrow">QUẢN TRỊ TÀI KHOẢN</span><h1>${creating ? 'Tạo người dùng mới' : 'Cập nhật người dùng'}</h1><p>${creating ? 'Thêm tài khoản vào hệ thống.' : 'Chỉnh sửa thông tin và quyền truy cập.'}</p></div>
        <c:if test="${not empty error}"><div class="alert error"><c:out value="${error}"/></div></c:if>
        <form class="stack-form admin-form" method="post" action="<c:url value='/admin/users/form'/>">
            <input type="hidden" name="csrfToken" value="${videoCsrf}">
            <input type="hidden" name="creating" value="${creating}">
            <div class="field-row">
                <label>Tên đăng nhập<input name="username" minlength="3" maxlength="50" pattern="[A-Za-z0-9._\-]{3,50}" required ${creating ? '' : 'readonly'} value="<c:out value='${formUser.username}'/>" placeholder="Tên đăng nhập"></label>
                <label>Họ và tên<input name="fullname" minlength="2" maxlength="50" required value="<c:out value='${formUser.fullname}'/>" placeholder="Nguyễn Văn An"></label>
            </div>
            <div class="field-row">
                <label>Email<input type="email" name="email" maxlength="150" required value="<c:out value='${formUser.email}'/>" placeholder="name@example.com"></label>
                <label>Số điện thoại<input type="tel" name="phone" maxlength="12" pattern="(0|\+84)[35789][0-9]{8}" value="<c:out value='${formUser.phone}'/>" placeholder="0912345678"></label>
            </div>
            <label>${creating ? 'Mật khẩu' : 'Mật khẩu mới (để trống nếu giữ nguyên)'}<input type="password" name="password" ${creating ? 'required' : ''} minlength="6" maxlength="128" placeholder="Tối thiểu 6 ký tự"></label>
            <label>Đường dẫn ảnh<input name="images" maxlength="500" value="<c:out value='${formUser.images}'/>" placeholder="/assets/images/avatar-user.svg"></label>
            <div class="toggle-row">
                <label class="toggle"><input type="checkbox" name="active" ${formUser.active ? 'checked' : ''}><span></span><div><strong>Kích hoạt</strong><small>Cho phép tài khoản đăng nhập</small></div></label>
                <label class="toggle"><input type="checkbox" name="admin" ${formUser.admin ? 'checked' : ''}><span></span><div><strong>Quyền Admin</strong><small>Cho phép vào trang quản trị</small></div></label>
            </div>
            <div class="form-actions"><a class="button ghost" href="<c:url value='/admin/users'/>">Hủy</a><button class="button primary" type="submit">${creating ? 'Tạo người dùng' : 'Lưu thay đổi'}</button></div>
        </form>
    </section>
</main>
</body>
</html>
