package com.bupt.commerceinsight.common;

import java.util.List;

public class PageResponse<T> {

    private final int pageNo;
    private final int pageSize;
    private final long total;
    private final List<T> records;

    public PageResponse(int pageNo, int pageSize, long total, List<T> records) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
    }

    public int getPageNo() {
        return pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotal() {
        return total;
    }

    public List<T> getRecords() {
        return records;
    }
}
