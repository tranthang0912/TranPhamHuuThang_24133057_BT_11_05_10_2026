<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>Trang Chủ | Hữu Thắng Video</title></head>
<body>
<c:url var="posterFallback" value="/assets/images/poster-tech.svg"/>
<main>
    <c:if test="${not empty flashError}"><div class="container"><div class="alert error detail-flashes" role="alert"><c:out value="${flashError}"/></div></div></c:if>
    <c:if test="${not empty flashSuccess}"><div class="container"><div class="alert success detail-flashes" role="status"><c:out value="${flashSuccess}"/></div></div></c:if>
    <section class="hero">
        <div class="container hero-grid">
            <div class="hero-copy">
                <span class="eyebrow">THƯ VIỆN VIDEO &amp; HỌC TẬP</span>
                <h1>Khám phá nội dung<br><em>theo cách của bạn.</em></h1>
                <p>Thư viện video được tổ chức theo chủ đề, trình bày rõ ràng và tối ưu cho trải nghiệm xem trên mọi thiết bị.</p>
                <div class="hero-actions">
                    <a class="button primary" href="<c:url value='/videos'/>">Khám phá video <span>→</span></a>
                    <a class="button ghost" href="<c:url value='/shop'/>">Mua video học tập</a>
                </div>
                <div class="connection-state ${databaseAvailable ? 'online' : 'offline'}">
                    <span></span><b>${databaseAvailable ? 'Sẵn sàng khám phá video và học tập' : 'Thư viện đang tạm thời không khả dụng'}</b>
                </div>
            </div>
            <div class="hero-art" aria-hidden="true">
                <div class="orb orb-one"></div><div class="orb orb-two"></div>
                <div class="feature-card main-card"><span class="play-button">▶</span><div><small>Thư viện nổi bật</small><strong>Học • Xem • Chia sẻ</strong></div></div>
                <div class="mini-stat stat-one"><strong>03</strong><span>Chủ đề</span></div>
                <div class="mini-stat stat-two"><strong>10+</strong><span>Video</span></div>
            </div>
        </div>
    </section>

    <section class="container section-space">
        <div class="section-heading split-heading">
            <div><span class="eyebrow">DANH MỤC</span><h2>Tìm đúng chủ đề bạn quan tâm</h2></div>
            <a class="text-link" href="<c:url value='/videos'/>">Xem tất cả →</a>
        </div>
        <div class="category-grid">
            <c:forEach items="${categories}" var="category" varStatus="status">
                <c:url var="categoryUrl" value="/videos"><c:param name="categoryId" value="${category.categoryId}"/></c:url>
                <a class="category-tile" href="${categoryUrl}">
                    <span class="category-index"><c:if test="${status.index + 1 < 10}">0</c:if>${status.index + 1}</span>
                    <div><strong><c:out value="${category.categoryName}"/></strong><small><c:out value="${category.videoCount}"/> video</small></div>
                    <span class="circle-arrow">↗</span>
                </a>
            </c:forEach>
        </div>
    </section>

    <section class="soft-section">
        <div class="container section-space">
            <div class="section-heading"><span class="eyebrow">MỚI NHẤT</span><h2>Video dành cho bạn</h2></div>
            <div class="video-grid">
                <c:forEach items="${featuredVideos}" var="video">
                    <c:url var="detailUrl" value="/videos/detail"><c:param name="id" value="${video.videoId}"/></c:url>
                    <c:url var="posterUrl" value="${video.poster}"/>
                    <article class="video-card">
                        <a class="poster-wrap" href="${detailUrl}"><img src="${posterUrl}" data-fallback="${posterFallback}" alt="<c:out value='${video.title}'/>" loading="lazy" decoding="async"><span class="poster-play">▶</span></a>
                        <div class="video-card-body">
                            <span class="video-category"><c:out value="${video.categoryName}"/></span>
                            <h3><a href="${detailUrl}"><c:out value="${video.title}"/></a></h3>
                            <div class="video-stats"><span>◉ <b><c:out value="${video.views}"/></b> lượt xem</span><span>♥ <b><c:out value="${video.likeCount}"/></b></span></div>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </div>
    </section>
</main>
</body>
</html>
