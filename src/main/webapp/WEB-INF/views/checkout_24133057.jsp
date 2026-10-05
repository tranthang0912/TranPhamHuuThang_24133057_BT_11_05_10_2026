<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html lang="vi"><head><title>Thanh toán COD | Hữu Thắng</title></head><body>
<fmt:setLocale value="vi_VN"/>
<main class="container commerce-page">
    <div class="commerce-heading"><div><span class="eyebrow">HOÀN TẤT ĐƠN HÀNG</span><h1>Thanh toán COD</h1><p>Kiểm tra thông tin nhận hàng và tổng tiền trước khi đặt đơn.</p></div><a class="button outline" href="<c:url value='/cart'/>">Sửa giỏ hàng</a></div>
    <c:if test="${not empty errors}"><div class="alert error" role="alert">Chưa thể đặt đơn. Vui lòng kiểm tra các thông tin bên dưới.<c:if test="${not empty errors.cart}"><p><c:out value="${errors.cart}"/></p></c:if></div></c:if>
    <div class="commerce-columns"><section class="commerce-panel"><h2>Thông tin nhận hàng</h2>
        <form class="stack-form checkout-form" method="post" action="<c:url value='/checkout'/>" data-checkout-form>
            <input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="checkoutToken" value="${checkoutToken}">
            <label for="recipient">Họ tên người nhận *</label><input id="recipient" name="recipient" value="<c:out value='${form.recipient}'/>" minlength="2" maxlength="100" autocomplete="name" required aria-invalid="${not empty errors.recipient}" aria-describedby="recipient-error">
            <small id="recipient-error" class="field-error"><c:out value="${errors.recipient}"/></small>
            <label for="phone">Số điện thoại *</label><input id="phone" name="phone" type="tel" value="<c:out value='${form.phone}'/>" pattern="(0|\+84)[35789][0-9]{8}" maxlength="12" placeholder="0912345678" autocomplete="tel" required aria-invalid="${not empty errors.phone}" aria-describedby="phone-error">
            <small id="phone-error" class="field-error"><c:out value="${errors.phone}"/></small>
            <label for="address">Địa chỉ nhận hàng *</label><textarea id="address" name="address" rows="3" minlength="10" maxlength="500" autocomplete="street-address" placeholder="Số nhà, tên đường, phường/xã, tỉnh/thành phố" required aria-invalid="${not empty errors.address}" aria-describedby="address-error"><c:out value="${form.address}"/></textarea>
            <small id="address-error" class="field-error"><c:out value="${errors.address}"/></small>
            <label for="note">Ghi chú (không bắt buộc)</label><textarea id="note" name="note" rows="2" maxlength="500" aria-invalid="${not empty errors.note}" aria-describedby="note-error"><c:out value="${form.note}"/></textarea><small id="note-error" class="field-error"><c:out value="${errors.note}"/></small>
            <div class="cod-option"><input type="radio" id="cod" name="paymentMethod" value="COD" checked required><label for="cod"><strong>Thanh toán khi nhận hàng (COD)</strong><span>Thanh toán đủ tiền khi nhận hàng. Sau đó, mã kích hoạt được gửi tới email tài khoản và hiển thị trong chi tiết đơn.</span></label></div>
            <c:if test="${not empty errors.paymentMethod}"><small class="field-error"><c:out value="${errors.paymentMethod}"/></small></c:if>
            <c:choose><c:when test="${cart.checkoutAllowed}"><button class="button primary full" type="submit" data-place-order>Đặt đơn COD — <fmt:formatNumber value="${cart.total}" maxFractionDigits="0"/> ₫</button></c:when><c:otherwise><button class="button outline full" disabled>Vui lòng sửa giỏ hàng trước</button></c:otherwise></c:choose>
        </form>
    </section><aside class="commerce-panel order-summary"><h2>Đơn hàng của bạn</h2>
        <c:forEach items="${cart.lines}" var="line"><div class="checkout-line"><div><strong><c:out value="${line.product.title}"/></strong><small>${line.quantity} bộ × <fmt:formatNumber value="${line.product.price}" maxFractionDigits="0"/> ₫</small></div><b><fmt:formatNumber value="${line.subtotal}" maxFractionDigits="0"/> ₫</b></div></c:forEach>
        <div class="summary-row"><span>Phí giao hàng</span><strong>Miễn phí</strong></div><div class="summary-row summary-total"><span>Tổng phải trả</span><strong><fmt:formatNumber value="${cart.total}" maxFractionDigits="0"/> ₫</strong></div><p class="muted">Số lượng và giá đã lưu trong giỏ sẽ được đối chiếu lại khi đặt đơn.</p>
    </aside></div>
</main></body></html>
