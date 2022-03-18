package com.example.answersboxapi.model;

public enum SortParams {
    CREATED_UP("+createdAt"), CREATED_DOWN("-createdAt"),
    UPDATED_UP("+updatedAt"), UPDATE_DOWN("-updatedAt"),
    RATING_UP("+rating"), RATING_DOWN("-rating"),
    DELETED_UP("+deletedAt"), DELETED_DOWN("-deletedAt");

    private String sortParam;

    SortParams(String direction) {
        this.sortParam = direction;
    }

    public String getSortParam() {
        return sortParam;
    }
}
