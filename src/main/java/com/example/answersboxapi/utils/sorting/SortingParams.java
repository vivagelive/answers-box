package com.example.answersboxapi.utils.sorting;

public enum SortingParams {
    DEFAULT("default"),
    CREATED_UP("+createdAt"), CREATED_DOWN("-createdAt"),
    UPDATED_UP("+updatedAt"), UPDATE_DOWN("-updatedAt"),
    RATING_UP("+rating"), RATING_DOWN("-rating"),
    DELETED_UP("+deletedAt"), DELETED_DOWN("-deletedAt");

    private String sortParam;

    SortingParams(String direction) {
        this.sortParam = direction;
    }
     public String getSortParam() {
        return sortParam;
     }
}
