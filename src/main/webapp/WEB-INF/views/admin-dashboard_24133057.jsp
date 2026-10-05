<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>Trang quản trị | 24133057</title></head>
<body>
<main>
    <section class="admin-banner">
        <div class="container admin-welcome">
            <div><span class="eyebrow light">TRANG QUẢN TRỊ</span><h1>Xin chào, <c:out value="${currentUser.fullname}"/></h1><p>Theo dõi hoạt động thư viện và quản lý tài khoản.</p></div>
            <a class="button light-button" href="<c:url value='/admin/users/form'/>">＋ Thêm người dùng</a>
        </div>
    </section>
    <section class="container admin-content">
        <div class="stat-grid">
            <article class="stat-card purple"><span>TÀI KHOẢN</span><strong><c:out value="${totalUsers}"/></strong><small>Tài khoản hệ thống</small></article>
            <article class="stat-card coral"><span>VIDEO</span><strong><c:out value="${totalVideos}"/></strong><small>Video đang hoạt động</small></article>
            <article class="stat-card teal"><span>DANH MỤC</span><strong><c:out value="${totalCategories}"/></strong><small>Danh mục nội dung</small></article>
        </div>
        <div class="dashboard-grid">
            <section class="panel">
                <div class="panel-heading"><div><span class="eyebrow">QUẢN LÝ</span><h2>Truy cập nhanh</h2></div></div>
                <div class="quick-links">
                    <a href="<c:url value='/admin/orders'/>"><span>▣</span><div><strong>Quản lý đơn hàng</strong><small>Xác nhận đơn, thu tiền COD và lấy mã video</small></div><b>→</b></a>
                    <a href="<c:url value='/admin/users'/>"><span>◎</span><div><strong>Quản lý người dùng</strong><small>Thông tin và quyền truy cập của tài khoản</small></div><b>→</b></a>
                    <a href="<c:url value='/videos'/>"><span>▷</span><div><strong>Kho video</strong><small>Khám phá video theo chủ đề</small></div><b>→</b></a>
                    <a href="<c:url value='/system/jsp'/>"><span>◇</span><div><strong>Kiểm tra JSP</strong><small>Servlet + JDBC + JSP compatibility</small></div><b>→</b></a>
                </div>
            </section>
            <section class="panel recent-panel">
                <div class="panel-heading"><div><span class="eyebrow">NỘI DUNG</span><h2>Video mới</h2></div><a href="<c:url value='/videos'/>">Xem tất cả</a></div>
                <c:forEach items="${recentVideos}" var="video">
                    <c:url var="detailUrl" value="/videos/detail"><c:param name="id" value="${video.videoId}"/></c:url>
                    <c:url var="posterUrl" value="${video.poster}"/>
                    <a class="recent-video" href="${detailUrl}"><img src="${posterUrl}" alt=""><div><strong><c:out value="${video.title}"/></strong><small><c:out value="${video.categoryName}"/> • <c:out value="${video.views}"/> lượt xem</small></div><span>↗</span></a>
                </c:forEach>
            </section>
        </div>
    </section>
</main>
</body>
</html>
