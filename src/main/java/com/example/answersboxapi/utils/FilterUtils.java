package com.example.answersboxapi.utils;

import lombok.experimental.UtilityClass;

import static com.example.answersboxapi.utils.SecurityUtils.isAdmin;

@UtilityClass
public class FilterUtils {

    public static boolean createDeletedFlagDefault(final boolean isDeleted) {
        if (isAdmin()) {
            return isDeleted;
        }
        return false;
    }
}
