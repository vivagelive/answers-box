package com.example.answersboxapi.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ExceptionHandling extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Object> handle(EntityAlreadyProcessedException exception) {
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), exception.getStatus());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handle(UnauthorizedException exception) {
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), exception.getStatus());
    }

    @ExceptionHandler(TokenNotValidException.class)
    public ResponseEntity<Object> handle(TokenNotValidException exception) {
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), exception.getStatus());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handle(EntityNotFoundException exception) {
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), exception.getStatus());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handle(AccessDeniedException exception) {
        return new ResponseEntity<>(exception.getMessage(), new HttpHeaders(), exception.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        final List<UnexpectedException> errors =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(error -> {
                            UnexpectedException unexpectedException = new UnexpectedException();
                            unexpectedException.setMessage(error.getDefaultMessage());
                            return unexpectedException;
                        })
                        .collect(Collectors.toList());
        return new ResponseEntity<>(errors, headers, status);
    }
}
