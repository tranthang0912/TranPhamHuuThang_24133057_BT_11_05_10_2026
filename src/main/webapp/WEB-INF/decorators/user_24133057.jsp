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
<body>
<header class="site-header">
    <div class="container nav-shell">
        <a class="brand" href="${pageContext.request.contextPath}/home" aria-label="V&#7873; trang ch&#7911;">
            <span class="brand-mark">H</span>
            <span><strong>H&#7919;u Th&#7855;ng</strong><small>Video Library</small></span>
        </a>
        <button class="nav-toggle" type="button" aria-label="M&#7903; menu" aria-expanded="false" aria-controls="main-navigation" data-nav-toggle>&#9776;</button>
        <nav id="main-navigation" class="main-nav" data-main-nav aria-label="Điều hướng chính">
            <a class="${currentPath == '/home' ? 'active' : ''}" href="${pageContext.request.contextPath}/home">Trang chủ</a>
            <a class="${currentPath == '/videos' or currentPath == '/videos/detail' ? 'active' : ''}" href="${pageContext.request.contextPath}/videos">Thư viện video</a>
            <a class="${currentPath == '/shop' ? 'active' : ''}" href="${pageContext.request.contextPath}/shop">Mua video</a>
            <a class="${currentPath == '/cart' or currentPath == '/checkout' ? 'active' : ''}" href="${pageContext.request.contextPath}/cart">Giỏ hàng</a>
            <c:choose>
                <c:when test="${not empty sessionScope.currentUser}">
                    <a class="${currentPath == '/account/videos' or currentPath == '/account/activate' ? 'active' : ''}" href="${pageContext.request.contextPath}/account/videos">Video của tôi</a>
                    <a class="${currentPath == '/videos/favorites' ? 'active' : ''}" href="${pageContext.request.contextPath}/videos/favorites">Yêu thích</a>
                    <a class="${currentPath == '/orders' or currentPath == '/orders/detail' ? 'active' : ''}" href="${pageContext.request.contextPath}/orders">Đơn của tôi</a>
                    <c:if test="${sessionScope.currentUser.admin}">
                        <a class="nav-admin" href="${pageContext.request.contextPath}/admin">Trang qu&#7843;n tr&#7883;</a>
                    </c:if>
                    <a class="nav-profile" href="${pageContext.request.contextPath}/account/profile">
                        <span class="nav-avatar">${sessionScope.currentUser.admin ? 'A' : 'U'}</span>
                        <span><c:out value="${sessionScope.currentUser.fullname}"/></span>
                    </a>
                    <a class="nav-logout" href="${pageContext.request.contextPath}/auth/logout">&#272;&#259;ng xu&#7845;t</a>
                </c:when>
                <c:otherwise>
                    <a class="nav-login" href="${pageContext.request.contextPath}/auth/login">&#272;&#259;ng nh&#7853;p</a>
                </c:otherwise>
            </c:choose>
        </nav>
    </div>
</header>
<div class="page-content"><sitemesh:write property="body"/></div>
<footer class="site-footer">
    <div class="container footer-grid">
        <div><strong>Tr&#7847;n Ph&#7841;m H&#7919;u Th&#7855;ng</strong><span>B&#224;i thi L&#7853;p Tr&#236;nh Web</span></div>
        <div class="footer-meta"><span>MSSV: <b>24133057</b></span><span>M&#227; &#273;&#7873;: <b>04</b></span></div>
    </div>
</footer>
<script src="${pageContext.request.contextPath}/assets/js/app.js?v=3"></script>
<script src="${pageContext.request.contextPath}/assets/js/commerce.js?v=3"></script>
<script src="${pageContext.request.contextPath}/assets/js/video.js?v=3"></script>
</body>
</html>
