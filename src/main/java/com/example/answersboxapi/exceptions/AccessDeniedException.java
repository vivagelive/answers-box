package com.example.answersboxapi.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    public AccessDeniedException (final String message) {
        super(message);
        this.status = HttpStatus.FORBIDDEN;
    }
}
