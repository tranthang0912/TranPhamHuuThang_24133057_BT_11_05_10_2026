<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi"><head><title>Quản lý đơn hàng | Hữu Thắng</title></head><body>
<fmt:setLocale value="vi_VN"/><fmt:setTimeZone value="Asia/Ho_Chi_Minh"/>
<main class="container commerce-page">
    <div class="commerce-heading"><div><span class="eyebrow">QUẢN TRỊ</span><h1>Quản lý đơn hàng</h1><p>1. Xác nhận đơn → 2. Đã thu tiền COD — cấp mã → 3. Sao chép mã để kích hoạt video.</p></div><a class="button outline" href="<c:url value='/admin/orders'/>">Danh sách đơn hàng</a></div>
    <c:if test="${not empty flashSuccess}"><div class="alert success" role="status"><c:out value="${flashSuccess}"/></div></c:if>
    <c:if test="${not empty flashError}"><div class="alert error" role="alert"><c:out value="${flashError}"/></div></c:if>
    <c:choose><c:when test="${not empty order}">
        <section class="commerce-panel">
            <div class="order-section-heading"><h2>Đơn #${order.orderId}</h2><span class="order-status status-${order.status.code}"><c:out value="${order.status.label}"/></span></div>
            <p>Khách hàng: <strong><c:out value="${order.username}"/></strong> • <c:out value="${order.recipient}"/> • <c:out value="${order.phone}"/></p>
            <p class="muted"><c:out value="${order.address}"/></p>
            <c:forEach items="${order.items}" var="item"><div class="summary-line"><div><strong><c:out value="${item.title}"/></strong><small>Số lượng: ${item.quantity}</small></div><b><fmt:formatNumber value="${item.subtotal}" maxFractionDigits="0"/> ₫</b></div></c:forEach>
            <div class="summary-row summary-total"><span>Tổng đơn hàng</span><strong><fmt:formatNumber value="${order.total}" maxFractionDigits="0"/> ₫</strong></div>
            <p><strong><c:out value="${order.paymentLabel}"/></strong></p>
            <c:if test="${not empty order.status.nextStatuses}"><form class="stack-form" method="post" action="<c:url value='/admin/orders'/>" style="margin-bottom:20px;max-width:480px"><input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="id" value="${order.orderId}"><label for="next-status">Chuyển trạng thái đơn hàng</label><select id="next-status" name="action" required><c:forEach items="${order.status.nextStatuses}" var="next"><option value="${next.code}"><c:out value="${next.label}"/></option></c:forEach></select><button class="button outline" type="submit">Cập nhật trạng thái</button><small class="muted">“Đã giao” xác nhận đã thu tiền COD và cấp mã. Hủy/hoàn sẽ hoàn số lượng; hoàn đơn thu hồi quyền xem.</small></form></c:if>
            <c:if test="${order.status.code == 'NEW'}"><form method="post" action="<c:url value='/admin/orders'/>"><input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="id" value="${order.orderId}"><input type="hidden" name="action" value="confirm"><button class="button primary" type="submit">✓ Xác nhận đơn</button></form></c:if>
            <c:if test="${order.status.code == 'CONFIRMED' or order.status.code == 'PREPARING' or order.status.code == 'SHIPPING' or order.status.code == 'OUT_FOR_DELIVERY'}"><form method="post" action="<c:url value='/admin/orders'/>"><input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="id" value="${order.orderId}"><input type="hidden" name="action" value="collect-cod"><button class="button primary" type="submit">Đã thu tiền COD — cấp mã</button></form><p class="muted">Nhấn khi đã thu tiền, hoặc dùng để mô phỏng hoàn tất COD khi test.</p></c:if>
        </section>
        <section class="commerce-panel activation-codes" style="margin-top:24px"><h2>Mã kích hoạt video</h2>
            <c:choose><c:when test="${not empty activationCodes}">
                <c:forEach items="${activationCodes}" var="code" varStatus="row"><div class="activation-code-row"><div class="order-section-heading"><strong><c:out value="${code.title}"/></strong><span class="code-status"><c:out value="${code.statusLabel}"/></span></div><c:if test="${code.status == 'AVAILABLE'}"><div class="code-copy-row"><label class="visually-hidden" for="admin-code-${row.index}">Mã kích hoạt</label><input id="admin-code-${row.index}" class="code-input" value="<c:out value='${code.code}'/>" readonly><button class="button outline" type="button" data-copy-code="admin-code-${row.index}">Sao chép</button><span class="input-hint" role="status" data-code-feedback></span></div></c:if></div></c:forEach>
                <p class="muted">Khách đặt hàng có thể lấy mã ngay trong Đơn của tôi → Chi tiết đơn, không cần chờ email.</p><a class="button outline" href="<c:url value='/account/activate'/>">Nhập mã kích hoạt video</a>
            </c:when><c:otherwise><p class="muted">Mã được cấp sau khi xác nhận đơn và nhấn “Đã thu tiền COD — cấp mã”. Đơn đã hủy không được cấp mã.</p></c:otherwise></c:choose>
        </section>
        <section class="commerce-panel" style="margin-top:24px"><h2>Tiến trình đơn hàng</h2><ol class="order-timeline"><c:forEach items="${order.events}" var="event"><li><span class="timeline-dot"></span><div><strong><c:out value="${event.status.label}"/></strong><small><fmt:formatDate value="${event.changedAt}" pattern="dd/MM/yyyy HH:mm:ss"/></small></div></li></c:forEach></ol></section>
    </c:when><c:otherwise>
        <form class="stack-form" method="get" action="<c:url value='/admin/orders'/>" style="margin-bottom:24px;max-width:480px"><label for="status-filter">Lọc trạng thái</label><select id="status-filter" name="status"><option value="">Tất cả trạng thái</option><c:forEach items="${statuses}" var="status"><option value="${status.code}" ${selectedStatus == status.code ? 'selected' : ''}><c:out value="${status.label}"/></option></c:forEach></select><button class="button outline" type="submit">Lọc đơn hàng</button></form>
        <p class="muted">Hiển thị 50 đơn gần nhất. Mở đơn để xác nhận và cấp mã.</p>
        <c:choose><c:when test="${empty orders}"><div class="commerce-empty"><h2>Chưa có đơn hàng</h2><p>Đặt một đơn COD trong cửa hàng, sau đó quay lại đây để xác nhận.</p><a class="button primary" href="<c:url value='/shop'/>">Mua video</a></div></c:when><c:otherwise><div class="order-list">
            <c:forEach items="${orders}" var="entry"><c:url var="detailUrl" value="/admin/orders"><c:param name="id" value="${entry.orderId}"/></c:url><article class="commerce-panel order-card"><div><a class="order-number" href="${detailUrl}">Đơn #${entry.orderId}</a><p><strong><c:out value="${entry.username}"/></strong> • <c:out value="${entry.recipient}"/></p><p class="muted"><fmt:formatDate value="${entry.createdAt}" pattern="dd/MM/yyyy HH:mm"/> • <c:out value="${entry.paymentLabel}"/></p></div><div class="order-card-right"><span class="order-status status-${entry.status.code}"><c:out value="${entry.status.label}"/></span><strong><fmt:formatNumber value="${entry.total}" maxFractionDigits="0"/> ₫</strong><a class="button outline small" href="${detailUrl}">Xem và xử lý đơn</a></div></article></c:forEach>
        </div></c:otherwise></c:choose>
    </c:otherwise></c:choose>
</main></body></html>
