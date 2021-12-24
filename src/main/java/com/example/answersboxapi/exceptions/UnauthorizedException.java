package com.example.answersboxapi.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends RuntimeException {

    @Getter
    private final HttpStatus status;

    public UnauthorizedException(final String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED;
    }
}
