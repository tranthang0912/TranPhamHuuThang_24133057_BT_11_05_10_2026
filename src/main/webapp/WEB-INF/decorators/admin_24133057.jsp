<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><sitemesh:write property="title"/></title>
    <link rel="icon" type="image/svg+xml" href="${pageContext.request.contextPath}/assets/images/favicon.svg">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link rel="preconnect" href="https://i.ytimg.com">
    <link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;600;700;800&amp;display=swap" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/profile.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/commerce.css?v=3">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui.css?v=3">
    <sitemesh:write property="head"/>
</head>
<body class="admin-body">
<header class="site-header admin-header">
    <div class="container nav-shell">
        <a class="brand" href="${pageContext.request.contextPath}/admin">
            <span class="brand-mark">A</span>
            <span><strong>Hữu Thắng</strong><small>Quản trị video</small></span>
        </a>
        <button class="nav-toggle" type="button" aria-label="Mở menu" aria-expanded="false" aria-controls="admin-navigation" data-nav-toggle>&#9776;</button>
        <nav id="admin-navigation" class="main-nav" data-main-nav>
            <a href="${pageContext.request.contextPath}/home">Trang Ch&#7911;</a>
            <a href="${pageContext.request.contextPath}/shop">Mua video</a>
            <a href="${pageContext.request.contextPath}/cart">Giỏ hàng</a>
            <a href="${pageContext.request.contextPath}/orders">Đơn của tôi</a>
            <a href="${pageContext.request.contextPath}/account/videos">Video của tôi</a>
            <a class="nav-admin" href="${pageContext.request.contextPath}/admin">Trang qu&#7843;n tr&#7883;</a>
            <a href="${pageContext.request.contextPath}/admin/users">Người dùng</a>
            <a class="${currentPath == '/admin/orders' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/orders">Quản lý đơn hàng</a>
            <a href="${pageContext.request.contextPath}/videos/favorites">Yêu thích</a>
            <a class="nav-profile" href="${pageContext.request.contextPath}/account/profile">
                <span class="nav-avatar">A</span><span><c:out value="${sessionScope.currentUser.fullname}"/></span>
            </a>
            <a class="nav-logout" href="${pageContext.request.contextPath}/auth/logout">&#272;&#259;ng xu&#7845;t</a>
        </nav>
    </div>
</header>
<div class="page-content"><sitemesh:write property="body"/></div>
<footer class="site-footer admin-footer">
    <div class="container footer-grid">
        <div><strong>Tr&#7847;n Ph&#7841;m H&#7919;u Th&#7855;ng</strong><span>Admin workspace</span></div>
        <div class="footer-meta"><span>MSSV: <b>24133057</b></span><span>M&#227; &#273;&#7873;: <b>04</b></span></div>
    </div>
</footer>
<script src="${pageContext.request.contextPath}/assets/js/app.js?v=3"></script>
<script src="${pageContext.request.contextPath}/assets/js/commerce.js?v=3"></script>
<script src="${pageContext.request.contextPath}/assets/js/video.js?v=3"></script>
</body>
</html>
