<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html lang="vi"><head><title>Mua video học tập | Hữu Thắng</title></head><body>
<fmt:setLocale value="vi_VN"/>
<main class="container commerce-page">
    <div class="commerce-heading"><div><span class="eyebrow">HỌC TẬP MỖI NGÀY</span><h1>Cửa hàng video học tập</h1><p>Thanh toán COD, nhận mã qua email và kích hoạt để học. Mỗi bộ dùng cho một tài khoản.</p></div><a class="button outline" href="<c:url value='/cart'/>">Xem giỏ hàng</a></div>
    <c:if test="${not empty flashSuccess}"><div class="alert success" role="status"><c:out value="${flashSuccess}"/></div></c:if>
    <c:if test="${not empty flashError}"><div class="alert error" role="alert"><c:out value="${flashError}"/></div></c:if>
    <c:if test="${empty products}"><div class="commerce-empty"><h2>Chưa có video đang bán</h2><p>Vui lòng quay lại sau.</p></div></c:if>
    <div class="shop-grid">
        <c:forEach items="${products}" var="product">
            <c:url var="previewUrl" value="/videos/detail"><c:param name="id" value="${product.videoId}"/></c:url>
            <c:url var="poster" value="${product.poster}"/>
            <c:url var="fallback" value="/assets/images/poster-study.svg"/>
            <article class="shop-card">
                <a class="shop-poster" href="${previewUrl}"><img src="<c:out value='${poster}'/>" data-fallback="${fallback}" alt="<c:out value='${product.title}'/>" loading="lazy"><span>Xem chi tiết</span></a>
                <div class="shop-card-content"><h2><c:out value="${product.title}"/></h2><p class="shop-description"><c:out value="${product.description}"/></p>
                    <div class="shop-price"><strong><fmt:formatNumber value="${product.price}" maxFractionDigits="0"/> ₫</strong><span>/ bộ video</span></div>
                    <p class="stock-note">${product.stock > 0 ? 'Còn' : 'Hết hàng'} <c:if test="${product.stock > 0}"><c:out value="${product.stock}"/> bộ • tối đa ${product.maxQuantity} bộ/đơn</c:if></p>
                    <c:choose>
                        <c:when test="${product.stock == 0}"><button class="button outline full" disabled>Đã hết hàng</button></c:when>
                        <c:when test="${empty currentUser}"><a class="button primary full" href="<c:url value='/cart'/>">Đăng nhập để mua</a></c:when>
                        <c:otherwise>
                            <form method="post" action="<c:url value='/cart'/>" class="shop-add-form">
                                <input type="hidden" name="csrfToken" value="${csrfToken}"><input type="hidden" name="action" value="add"><input type="hidden" name="productId" value="${product.productId}">
                                <label for="quantity-${product.productId}" class="visually-hidden">Số lượng <c:out value="${product.title}"/></label>
                                <input id="quantity-${product.productId}" type="number" name="quantity" min="1" max="${product.maxQuantity}" step="1" value="1" required>
                                <button class="button primary" type="submit">Thêm vào giỏ</button>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </div>
            </article>
        </c:forEach>
    </div>
</main></body></html>
