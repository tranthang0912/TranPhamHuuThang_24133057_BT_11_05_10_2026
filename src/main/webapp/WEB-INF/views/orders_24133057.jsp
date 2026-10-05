<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html lang="vi"><head><title>Lịch sử đặt hàng | Hữu Thắng</title></head><body>
<fmt:setLocale value="vi_VN"/><fmt:setTimeZone value="Asia/Ho_Chi_Minh"/>
<main class="container commerce-page">
    <div class="commerce-heading"><div><span class="eyebrow">ĐƠN HÀNG CỦA BẠN</span><h1>Lịch sử đặt hàng</h1><p>Theo dõi trạng thái hiện tại và xem chi tiết từng đơn.</p></div><a class="button outline" href="<c:url value='/orders'><c:param name='status' value='${selectedStatus}'/><c:param name='page' value='${pageNumber}'/></c:url>">Làm mới trạng thái</a></div>
    <c:if test="${not empty flashSuccess}"><div class="alert success" role="status"><c:out value="${flashSuccess}"/></div></c:if>
    <c:if test="${not empty flashError}"><div class="alert error" role="alert"><c:out value="${flashError}"/></div></c:if>
    <nav class="order-filters" aria-label="Lọc trạng thái đơn hàng"><a class="${empty selectedStatus ? 'selected' : ''}" href="<c:url value='/orders'/>">Tất cả <span>${counts.ALL}</span></a>
        <c:forEach items="${statuses}" var="status"><c:url var="filterUrl" value="/orders"><c:param name="status" value="${status.code}"/></c:url><a class="${selectedStatus == status.code ? 'selected' : ''}" href="${filterUrl}" aria-current="${selectedStatus == status.code ? 'page' : 'false'}"><c:out value="${status.label}"/> <span>${counts[status.code]}</span></a></c:forEach>
    </nav><p class="muted">${totalOrders} đơn phù hợp • Trang ${pageNumber}/${totalPages}</p>
    <c:choose><c:when test="${empty orders}"><div class="commerce-empty"><h2>Chưa có đơn hàng ở trạng thái này</h2><p>Chọn bộ lọc khác hoặc bắt đầu mua video học tập.</p><a class="button primary" href="<c:url value='/shop'/>">Mua video</a></div></c:when><c:otherwise>
        <div class="order-list"><c:forEach items="${orders}" var="order"><c:url var="detailUrl" value="/orders/detail"><c:param name="id" value="${order.orderId}"/></c:url>
            <article class="commerce-panel order-card"><div><a class="order-number" href="${detailUrl}">Đơn #${order.orderId}</a><p class="muted"><fmt:formatDate value="${order.createdAt}" pattern="dd/MM/yyyy HH:mm"/> • COD</p><p><c:out value="${order.paymentLabel}"/></p></div><div class="order-card-right"><span class="order-status status-${order.status.code}"><c:out value="${order.status.label}"/></span><strong><fmt:formatNumber value="${order.total}" maxFractionDigits="0"/> ₫</strong><a class="button outline small" href="${detailUrl}">Xem chi tiết</a></div></article>
        </c:forEach></div>
        <nav class="commerce-pagination" aria-label="Trang lịch sử đơn hàng"><c:if test="${pageNumber > 1}"><c:url var="prevUrl" value="/orders"><c:param name="status" value="${selectedStatus}"/><c:param name="page" value="${pageNumber - 1}"/></c:url><a class="button outline small" href="${prevUrl}">Trang trước</a></c:if><span>${pageNumber} / ${totalPages}</span><c:if test="${pageNumber < totalPages}"><c:url var="nextUrl" value="/orders"><c:param name="status" value="${selectedStatus}"/><c:param name="page" value="${pageNumber + 1}"/></c:url><a class="button outline small" href="${nextUrl}">Trang sau</a></c:if></nav>
    </c:otherwise></c:choose>
</main></body></html>
