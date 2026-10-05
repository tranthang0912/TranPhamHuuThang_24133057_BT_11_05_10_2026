<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head><title>Thư viện video | Hữu Thắng Video</title></head>
<body>
<c:url var="posterFallback" value="/assets/images/poster-tech.svg"/>
<main>
    <section class="page-banner">
        <div class="container">
            <span class="eyebrow light">THƯ VIỆN VIDEO</span>
            <h1><c:choose><c:when test="${not empty selectedCategory}"><c:out value="${selectedCategory.categoryName}"/></c:when><c:otherwise>Tất cả video</c:otherwise></c:choose></h1>
            <p>Khám phá video theo chủ đề, lưu nội dung yêu thích và học theo cách của bạn.</p>
        </div>
    </section>
    <section class="container section-space">
        <div class="category-tabs">
            <a class="${empty selectedCategoryId ? 'active' : ''}" href="<c:url value='/videos'/>">Tất cả</a>
            <c:forEach items="${categories}" var="category">
                <c:url var="categoryUrl" value="/videos"><c:param name="categoryId" value="${category.categoryId}"/></c:url>
                <a class="${selectedCategoryId == category.categoryId ? 'active' : ''}" href="${categoryUrl}"><span><c:out value="${category.categoryName}"/></span><b><c:out value="${category.videoCount}"/></b></a>
            </c:forEach>
        </div>
        <div class="list-summary">
            <div><span class="eyebrow">KẾT QUẢ</span><h2><c:choose><c:when test="${not empty selectedCategory}"><c:out value="${selectedCategory.categoryName}"/></c:when><c:otherwise>Tất cả</c:otherwise></c:choose> <small>(<c:out value="${videoPage.totalItems}"/>)</small></h2></div>
            <span class="page-indicator">Trang <c:out value="${videoPage.currentPage}"/>/<c:out value="${videoPage.totalPages}"/></span>
        </div>

        <c:choose>
            <c:when test="${not empty videoPage.items}">
                <div class="video-grid">
                    <c:forEach items="${videoPage.items}" var="video">
                        <c:url var="detailUrl" value="/videos/detail"><c:param name="id" value="${video.videoId}"/></c:url>
                        <c:url var="posterUrl" value="${video.poster}"/>
                        <article class="video-card">
                            <a class="poster-wrap" href="${detailUrl}"><img src="${posterUrl}" data-fallback="${posterFallback}" alt="<c:out value='${video.title}'/>" loading="lazy" decoding="async"><span class="poster-play">▶</span></a>
                            <div class="video-card-body">
                                <span class="video-category"><c:out value="${video.categoryName}"/></span>
                                <h3><a href="${detailUrl}"><c:out value="${video.title}"/></a></h3>
                                <p class="video-code">Mã video: <b><c:out value="${video.videoId}"/></b></p>
                                <div class="video-stats"><span>◉ <b><c:out value="${video.views}"/></b></span><span>↗ <b><c:out value="${video.shareCount}"/></b></span><span>♥ <b><c:out value="${video.likeCount}"/></b></span></div>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise><div class="empty-state"><span>◌</span><h3>Chưa có video</h3><p>Danh mục này chưa có nội dung đang hoạt động.</p></div></c:otherwise>
        </c:choose>

        <nav class="pagination" aria-label="Phân trang">
            <c:url var="previousUrl" value="/videos"><c:if test="${not empty selectedCategoryId}"><c:param name="categoryId" value="${selectedCategoryId}"/></c:if><c:param name="page" value="${videoPage.currentPage - 1}"/></c:url>
            <a class="${videoPage.hasPrevious ? '' : 'disabled'}" href="${previousUrl}">«</a>
            <c:forEach begin="1" end="${videoPage.totalPages}" var="pageNo">
                <c:url var="pageUrl" value="/videos"><c:if test="${not empty selectedCategoryId}"><c:param name="categoryId" value="${selectedCategoryId}"/></c:if><c:param name="page" value="${pageNo}"/></c:url>
                <a class="${pageNo == videoPage.currentPage ? 'active' : ''}" href="${pageUrl}">${pageNo}</a>
            </c:forEach>
            <c:url var="nextUrl" value="/videos"><c:if test="${not empty selectedCategoryId}"><c:param name="categoryId" value="${selectedCategoryId}"/></c:if><c:param name="page" value="${videoPage.currentPage + 1}"/></c:url>
            <a class="${videoPage.hasNext ? '' : 'disabled'}" href="${nextUrl}">»</a>
        </nav>
    </section>
</main>
</body>
</html>
