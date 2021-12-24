package com.example.answersboxapi.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class TokenNotValidException extends RuntimeException{

    @Getter
    private final HttpStatus status;

    public TokenNotValidException(final String message) {
        super(message);
        this.status = HttpStatus.UNAUTHORIZED;
    }
}
