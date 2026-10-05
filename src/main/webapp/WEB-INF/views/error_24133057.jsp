<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>Lỗi | Hữu Thắng Video</title></head>
<body>
<main class="center-page"><section class="error-card"><span class="error-code"><c:out value="${statusCode}"/></span><h1><c:out value="${errorTitle}"/></h1><p><c:out value="${errorMessage}"/></p><a class="button primary" href="<c:url value='/home'/>">Về trang chủ</a></section></main>
</body>
</html>
