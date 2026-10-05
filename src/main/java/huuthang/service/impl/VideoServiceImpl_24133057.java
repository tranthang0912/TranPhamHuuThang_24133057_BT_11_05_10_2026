package huuthang.service.impl;

import huuthang.dao.CategoryDao_24133057;
import huuthang.dao.VideoDao_24133057;
import huuthang.model.Category_24133057;
import huuthang.model.Page_24133057;
import huuthang.model.VideoView_24133057;
import huuthang.service.VideoService_24133057;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class VideoServiceImpl_24133057 implements VideoService_24133057 {
    private static final int VIDEOS_PER_PAGE = 3;

    private final VideoDao_24133057 videoDao;
    private final CategoryDao_24133057 categoryDao;

    public VideoServiceImpl_24133057(VideoDao_24133057 videoDao, CategoryDao_24133057 categoryDao) {
        this.videoDao = videoDao;
        this.categoryDao = categoryDao;
    }

    @Override
    public List<Category_24133057> getCategories() throws SQLException {
        return categoryDao.findAllWithVideoCount();
    }

    @Override
    public Optional<Category_24133057> getCategory(Integer categoryId) throws SQLException {
        return categoryId == null ? Optional.empty() : categoryDao.findById(categoryId);
    }

    @Override
    public Page_24133057<VideoView_24133057> getVideos(Integer categoryId, int page) throws SQLException {
        long total = videoDao.countByCategory(categoryId);
        int totalPages = Math.max(1, (int) Math.ceil((double) total / VIDEOS_PER_PAGE));
        int safePage = Math.min(Math.max(1, page), totalPages);
        List<VideoView_24133057> videos = videoDao.findPageByCategory(
                categoryId, (safePage - 1) * VIDEOS_PER_PAGE, VIDEOS_PER_PAGE);
        return new Page_24133057<>(videos, safePage, VIDEOS_PER_PAGE, total);
    }

    @Override
    public Optional<VideoView_24133057> getVideoDetailAndIncreaseView(String videoId) throws SQLException {
        return getVideoDetail(videoId, true);
    }

    @Override
    public Optional<VideoView_24133057> getVideoDetail(String videoId, boolean increaseView) throws SQLException {
        if (videoId == null || videoId.isBlank()) {
            return Optional.empty();
        }
        if (increaseView) videoDao.incrementViews(videoId.trim());
        return videoDao.findById(videoId.trim());
    }
}
