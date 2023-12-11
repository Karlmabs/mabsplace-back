package com.mabsplace.mabsplaceback.utils;

import lombok.Getter;

import java.util.List;

@Getter
public class PageDto<D> {
    private Integer limit;
    private Long totalItems;
    private Integer page;
    private Integer totalPages;
    private List<D> data;

    public PageDto() {
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public void setTotalItems(Long totalItems) {
        this.totalItems = totalItems;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public void setData(List<D> data) {
        this.data = data;
    }
}
