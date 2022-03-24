package com.example.answersboxapi.utils.pagination;

import com.example.answersboxapi.model.SortParams;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static com.example.answersboxapi.utils.sorting.SortingUtils.useSortOrDefault;

public class PagingUtils {

    public static PageRequest toPageRequest(final int page, final int size) {
        return PageRequest.of(page - 1, size, Sort.unsorted());
    }

    public static PageRequest toPageRequest(final int page, final int size, final Sort sort) {
        return PageRequest.of(page - 1, size, sort);
    }

    public static Pageable createPageableWithSort(final int page, final int size, final SortParams sortParams, Class<?> model) {
        final String sortParam = sortParams.getSortParam();
        final Sort sort = useSortOrDefault(sortParam, model);
        return toPageRequest(page, size, sort);
    }
}
