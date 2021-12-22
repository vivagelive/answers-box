package com.example.answersboxapi.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ExceptionHandling extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Object> handle(EntityAlreadyProcessedException exception) {
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), exception.getStatus());
    }
}
