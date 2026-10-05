package huuthang.model;

import java.util.Collections;
import java.util.List;

public class Page_24133057<T> {
    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final long totalItems;
    private final int totalPages;

    public Page_24133057(List<T> items, int currentPage, int pageSize, long totalItems) {
        this.items = items == null ? Collections.emptyList() : items;
        this.currentPage = Math.max(1, currentPage);
        this.pageSize = pageSize;
        this.totalItems = Math.max(0, totalItems);
        this.totalPages = Math.max(1, (int) Math.ceil((double) this.totalItems / pageSize));
    }

    public List<T> getItems() {
        return items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasPrevious() {
        return currentPage > 1;
    }

    public boolean isHasNext() {
        return currentPage < totalPages;
    }
}
