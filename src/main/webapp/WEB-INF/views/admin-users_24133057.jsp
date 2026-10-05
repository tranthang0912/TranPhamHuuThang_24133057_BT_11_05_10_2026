<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>Quản lý người dùng | 24133057</title></head>
<body>
<main class="container section-space admin-users-page">
    <div class="section-heading split-heading"><div><span class="eyebrow">QUẢN TRỊ TÀI KHOẢN</span><h1>Quản lý người dùng</h1><p>Quản lý thông tin, quyền truy cập và trạng thái tài khoản.</p></div><a class="button primary" href="<c:url value='/admin/users/form'/>">＋ Thêm người dùng</a></div>
    <c:if test="${not empty flashSuccess}"><div class="alert success"><c:out value="${flashSuccess}"/></div></c:if>
    <c:if test="${not empty flashError}"><div class="alert error"><c:out value="${flashError}"/></div></c:if>
    <section class="table-panel">
        <div class="table-summary"><span>Tổng cộng <strong><c:out value="${userPage.totalItems}"/></strong> tài khoản</span><span>Trang <c:out value="${userPage.currentPage}"/>/<c:out value="${userPage.totalPages}"/></span></div>
        <div class="table-scroll">
            <table class="data-table">
                <thead><tr><th>Người dùng</th><th>Liên hệ</th><th>Vai trò</th><th>Trạng thái</th><th>Thao tác</th></tr></thead>
                <tbody>
                <c:forEach items="${userPage.items}" var="user">
                    <c:url var="editUrl" value="/admin/users/form"><c:param name="username" value="${user.username}"/></c:url>
                    <tr>
                        <td><div class="user-cell"><span class="avatar"><c:out value="${fn:substring(user.fullname, 0, 1)}"/></span><div><strong><c:out value="${user.fullname}"/></strong><small>@<c:out value="${user.username}"/></small></div></div></td>
                        <td><strong class="cell-email"><c:out value="${user.email}"/></strong><small><c:out value="${user.phone}"/></small></td>
                        <td><span class="role-badge${user.admin ? ' admin' : ''}">${user.admin ? 'Quản trị viên' : 'Người dùng'}</span></td>
                        <td><span class="status-badge${user.active ? ' active' : ' inactive'}"><i></i><span>${user.active ? 'Đang hoạt động' : 'Chưa kích hoạt'}</span></span></td>
                        <td><div class="table-actions"><a title="Sửa" aria-label="Sửa tài khoản" href="${editUrl}">✎</a><form method="post" action="<c:url value='/admin/users/delete'/>" data-confirm="Xóa tài khoản này?"><input type="hidden" name="csrfToken" value="${videoCsrf}"><input type="hidden" name="username" value="<c:out value='${user.username}'/>"><button title="Xóa" aria-label="Xóa tài khoản" type="submit">⌫</button></form></div></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </section>
    <nav class="pagination" aria-label="Phân trang người dùng">
        <c:url var="previousUrl" value="/admin/users"><c:param name="page" value="${userPage.currentPage - 1}"/></c:url>
        <a class="${userPage.hasPrevious ? '' : 'disabled'}" href="${previousUrl}">«</a>
        <c:forEach begin="1" end="${userPage.totalPages}" var="pageNo">
            <c:url var="pageUrl" value="/admin/users"><c:param name="page" value="${pageNo}"/></c:url>
            <a class="${pageNo == userPage.currentPage ? 'active' : ''}" href="${pageUrl}">${pageNo}</a>
        </c:forEach>
        <c:url var="nextUrl" value="/admin/users"><c:param name="page" value="${userPage.currentPage + 1}"/></c:url>
        <a class="${userPage.hasNext ? '' : 'disabled'}" href="${nextUrl}">»</a>
    </nav>
</main>
</body>
</html>
