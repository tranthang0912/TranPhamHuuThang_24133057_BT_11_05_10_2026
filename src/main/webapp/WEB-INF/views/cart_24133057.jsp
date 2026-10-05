<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html lang="vi"><head><title>Giỏ hàng | Hữu Thắng</title></head><body>
<fmt:setLocale value="vi_VN"/>
<main class="container commerce-page">
    <div class="commerce-heading"><div><span class="eyebrow">VIDEO BẠN ĐÃ CHỌN</span><h1>Giỏ hàng</h1><p>Số lượng từ 1 đến 99, trong giới hạn còn bán. Bấm “Cập nhật” để lưu số lượng mới.</p></div><a class="button outline" href="<c:url value='/shop'/>">Tiếp tục mua</a></div>
    <c:if test="${not empty flashSuccess}"><div class="alert success" role="status"><c:out value="${flashSuccess}"/></div></c:if>
    <c:if test="${not empty flashError}"><div class="alert error" role="alert"><c:out value="${flashError}"/></div></c:if>
    <c:if test="${not empty error}"><div class="alert error" role="alert"><c:out value="${error}"/></div></c:if>
    <c:choose><c:when test="${empty cart.lines}"><div class="commerce-empty"><h2>Giỏ hàng đang trống</h2><p>Khám phá các video học tập và chọn nội dung phù hợp với bạn.</p><a class="button primary" href="<c:url value='/shop'/>">Chọn video</a></div></c:when>
    <c:otherwise><div class="commerce-columns"><section class="commerce-panel">
        <c:forEach items="${cart.lines}" var="line">
            <c:url var="poster" value="${line.product.poster}"/><c:url var="fallback" value="/assets/images/poster-study.svg"/>
            <article class="cart-row"><img class="cart-poster" src="<c:out value='${poster}'/>" data-fallback="${fallback}" alt="<c:out value='${line.product.title}'/>">
                <div class="cart-row-content"><h2><c:out value="${line.product.title}"/></h2><p><fmt:formatNumber value="${line.product.price}" maxFractionDigits="0"/> ₫ / bộ</p>
                    <c:if test="${not line.available}"><p class="field-error" role="alert">${line.product.active ? 'Số lượng vượt mức còn bán. Giảm số lượng hoặc xóa video.' : 'Video đã ngừng bán. Vui lòng xóa khỏi giỏ hàng.'}</p></c:if>
                    <c:if test="${line.product.active and line.product.stock > 0}">
                    <form class="quantity-form" method="post" action="<c:url value='/cart'/>">
                        <input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="action" value="update"><input type="hidden" name="productId" value="${line.product.productId}">
                        <label for="cart-qty-${line.product.productId}" class="visually-hidden">Số lượng <c:out value="${line.product.title}"/></label>
                        <div class="quantity-control"><button type="button" data-quantity-step="-1" aria-label="Giảm số lượng">−</button><input id="cart-qty-${line.product.productId}" name="quantity" type="number" min="1" max="${line.product.maxQuantity}" step="1" value="${line.quantity}" required><button type="button" data-quantity-step="1" aria-label="Tăng số lượng">+</button></div>
                        <button class="button outline small" type="submit">Cập nhật</button><small>Còn ${line.product.stock} bộ</small>
                    </form></c:if>
                    <form method="post" action="<c:url value='/cart'/>"><input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="action" value="remove"><input type="hidden" name="productId" value="${line.product.productId}"><button class="text-button danger" type="submit">Xóa khỏi giỏ</button></form>
                </div><strong class="cart-subtotal"><fmt:formatNumber value="${line.subtotal}" maxFractionDigits="0"/> ₫</strong>
            </article>
        </c:forEach>
        <form method="post" action="<c:url value='/cart'/>" data-confirm="Xóa tất cả video trong giỏ hàng?"><input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="action" value="clear"><button class="text-button danger" type="submit">Xóa toàn bộ giỏ hàng</button></form>
    </section><aside class="commerce-panel order-summary"><h2>Tóm tắt giỏ hàng</h2><div class="summary-row"><span>Số bộ video</span><strong>${cart.quantity}</strong></div><div class="summary-row"><span>Phí giao hàng</span><strong>Miễn phí</strong></div><div class="summary-row summary-total"><span>Tổng cộng</span><strong><fmt:formatNumber value="${cart.total}" maxFractionDigits="0"/> ₫</strong></div>
        <c:choose><c:when test="${cart.checkoutAllowed}"><a class="button primary full" href="<c:url value='/checkout'/>">Thanh toán COD</a></c:when><c:otherwise><button class="button outline full" disabled>Giỏ hàng cần cập nhật</button><p class="field-error">Xóa video ngừng bán/hết hàng hoặc giảm số lượng trước khi thanh toán.</p></c:otherwise></c:choose>
        <p class="muted">Giá và số lượng được kiểm tra lại khi xác nhận đơn.</p>
    </aside></div></c:otherwise></c:choose>
</main></body></html>
