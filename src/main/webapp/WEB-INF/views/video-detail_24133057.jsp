<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head><title><c:out value="${video.title}"/> | Hữu Thắng Video</title></head>
<body>
<c:url var="listUrl" value="/videos"><c:param name="categoryId" value="${video.categoryId}"/></c:url>
<c:url var="posterUrl" value="${video.poster}"/>
<c:url var="posterFallback" value="/assets/images/poster-tech.svg"/>
<main class="container section-space detail-page">
    <a class="back-link" href="${listUrl}">← Quay lại danh sách</a>
    <c:if test="${not empty flashSuccess}"><div class="alert success detail-flashes" role="status"><c:out value="${flashSuccess}"/></div></c:if>
    <c:if test="${not empty flashError}"><div class="alert error detail-flashes" role="alert"><c:out value="${flashError}"/></div></c:if>
    <section class="detail-grid">
        <c:choose>
            <c:when test="${not canWatch}">
                <div class="locked-video"><img src="${posterUrl}" data-fallback="${posterFallback}" alt="<c:out value='${video.title}'/>"><div class="locked-video-copy"><span class="lock-icon">♢</span><h2>Video dành cho học viên</h2><p>Thanh toán COD, nhận mã và kích hoạt để bắt đầu học.</p><a class="button primary" href="<c:url value='/account/activate'/>">Nhập mã kích hoạt</a><a class="locked-shop-link" href="<c:url value='/shop'/>">Chưa có mã? Mua video →</a></div></div>
            </c:when>
            <c:when test="${not empty video.youtubeId}">
                <div class="youtube-player">
                    <iframe src="https://www.youtube-nocookie.com/embed/${video.youtubeId}"
                            title="<c:out value='${video.title}'/>" loading="lazy"
                            referrerpolicy="strict-origin-when-cross-origin"
                            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                            allowfullscreen></iframe>
                </div>
            </c:when>
            <c:otherwise>
                <div class="detail-poster"><img src="${posterUrl}" data-fallback="${posterFallback}" alt="<c:out value='${video.title}'/>"><span class="detail-play">▶</span></div>
            </c:otherwise>
        </c:choose>
        <div class="detail-copy">
            <span class="video-category"><c:out value="${video.categoryName}"/></span>
            <h1><c:out value="${video.title}"/></h1>
            <div class="detail-code">Mã video: <strong><c:out value="${video.videoId}"/></strong></div>
            <div class="metric-row">
                <div><span>LƯỢT XEM</span><strong><c:out value="${video.views}"/></strong></div>
                <div><span>CHIA SẺ</span><strong data-share-count><c:out value="${video.shareCount}"/></strong></div>
                <div><span>YÊU THÍCH</span><strong><c:out value="${video.likeCount}"/></strong></div>
            </div>
            <div class="description-block"><h2>Mô tả</h2><p><c:out value="${video.description}"/></p></div>
            <div class="detail-actions">
                <c:if test="${canWatch and not empty video.youtubeId}"><a class="button youtube-button" href="https://www.youtube.com/watch?v=${video.youtubeId}" target="_blank" rel="noopener noreferrer">▶ Xem trên YouTube</a></c:if>
                <c:choose><c:when test="${not empty currentUser}"><form method="post" action="<c:url value='/videos/favorite'/>" data-favorite-form><input type="hidden" name="csrfToken" value="${videoCsrf}"><input type="hidden" name="id" value="<c:out value='${video.videoId}'/>"><input type="hidden" name="desired" value="${liked ? '0' : '1'}"><button class="button primary favorite-button ${liked ? 'is-liked' : ''}" type="submit" aria-pressed="${liked}"><span class="heart">${liked ? '♥' : '♡'}</span> ${liked ? 'Đã yêu thích' : 'Yêu thích'}</button></form></c:when><c:otherwise><a class="button primary" href="<c:url value='/videos/favorites'/>">♡ Yêu thích</a></c:otherwise></c:choose>
                <button class="button ghost" type="button" data-share-toggle aria-expanded="false" aria-controls="share-panel">↗ Chia sẻ</button>
            </div>
            <section id="share-panel" class="share-panel" hidden data-share-panel data-share-endpoint="<c:url value='/videos/share'/>" data-video-id="<c:out value='${video.videoId}'/>" data-csrf="${videoCsrf}" data-signed-in="${not empty currentUser}"><h2>Chia sẻ video này</h2><p>Sao chép đường dẫn và gửi cho bạn bè.</p><div class="share-copy-row"><label class="visually-hidden" for="share-url">Đường dẫn video</label><input id="share-url" readonly data-share-url><button class="button primary" type="button" data-share-copy>Sao chép</button></div><span class="share-feedback" role="status" aria-live="polite" data-share-feedback></span></section>
            <p class="interaction-note">Mỗi tài khoản có một lượt yêu thích. Nhấn “Đã yêu thích” để bỏ lưu.</p>
        </div>
    </section>
</main>
</body>
</html>
