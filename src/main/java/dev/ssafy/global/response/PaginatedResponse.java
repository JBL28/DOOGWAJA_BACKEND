package dev.ssafy.global.response;

import lombok.Getter;

import java.util.List;

@Getter
public class PaginatedResponse<T> {
    private final List<T> data;
    private final Pagination pagination;

    public PaginatedResponse(List<T> data, org.springframework.data.domain.Page<?> page) {
        this.data = data;
        this.pagination = new Pagination(
                page.getNumber() + 1, // 프론트는 1-index 기반
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Getter
    public static class Pagination {
        private final int page;
        private final int pageSize;
        private final long total;
        private final int totalPages;

        public Pagination(int page, int pageSize, long total, int totalPages) {
            this.page = page;
            this.pageSize = pageSize;
            this.total = total;
            this.totalPages = totalPages;
        }
    }
}
