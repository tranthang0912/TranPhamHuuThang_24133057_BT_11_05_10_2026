package huuthang.model;

public class VideoView_24133057 {
    private static final String YOUTUBE_THUMBNAIL_PREFIX = "https://i.ytimg.com/vi/";
    private String videoId;
    private String title;
    private String poster;
    private int views;
    private String description;
    private Boolean active;
    private Integer categoryId;
    private String categoryName;
    private int shareCount;
    private int likeCount;

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getYoutubeId() {
        if (poster == null || !poster.startsWith(YOUTUBE_THUMBNAIL_PREFIX)) {
            return null;
        }
        int idStart = YOUTUBE_THUMBNAIL_PREFIX.length();
        int idEnd = poster.indexOf('/', idStart);
        if (idEnd < 0) {
            return null;
        }
        String youtubeId = poster.substring(idStart, idEnd);
        return youtubeId.matches("[A-Za-z0-9_-]{11}") ? youtubeId : null;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActive() {
        return active;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
