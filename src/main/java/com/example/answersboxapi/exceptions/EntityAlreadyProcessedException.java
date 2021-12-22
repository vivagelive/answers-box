package com.example.answersboxapi.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class EntityAlreadyProcessedException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    public EntityAlreadyProcessedException(final String message) {
        super(message);
        this.status = HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
