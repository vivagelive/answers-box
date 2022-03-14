package com.example.answersboxapi.utils.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PagingUtils {

    public static PageRequest toPageRequest(final int page, final int size) {
        return PageRequest.of(page - 1, size, Sort.unsorted());
    }

    public static PageRequest toPageRequest(final int page, final int size, final Sort sort) {
        return PageRequest.of(page -1, size, sort);
    }
}
