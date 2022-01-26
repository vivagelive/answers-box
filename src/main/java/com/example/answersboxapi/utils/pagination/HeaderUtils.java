package com.example.answersboxapi.utils.pagination;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;


@UtilityClass
public class HeaderUtils {
    public static <T> MultiValueMap<String, String> generateHeaders(final Page<T> page) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Header", String.valueOf(page.getTotalElements()));
        return headers;
    }
}
