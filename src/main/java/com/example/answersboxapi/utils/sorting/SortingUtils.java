package com.example.answersboxapi.utils.sorting;

import com.example.answersboxapi.exceptions.InvalidInputDataException;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;

import java.util.Optional;

@UtilityClass
public class SortingUtils {

    public static Sort useSortOrDefault(final String sortParams, Class<?> model) {
        if (sortParams.equals("default")) {
            return Sort.by(Sort.Direction.DESC, "createdAt");  //default sort
        }
        return Sort.by(parseSort(sortParams, model));
    }

    private static Sort.Order parseSort(final String param, Class<?> model) {
        String direction = param.substring(0, 1);
        String column = param.substring(1);

        if(direction.equals("+") || direction.equals("-")) {
            validateSort(column, model);
            return direction.equals("+") ? Sort.Order.asc(column) : Sort.Order.desc((column));
        }
        throw new InvalidInputDataException("First string must be + or -");
    }

    private static void validateSort(final String sortParam, Class<?> model) {
        try {
            Optional.of(model.getDeclaredField(sortParam)).orElseThrow(NoSuchFieldException::new);
        } catch (NoSuchFieldException exception) {
            throw new InvalidInputDataException("Invalid column name");
        }
    }
}
