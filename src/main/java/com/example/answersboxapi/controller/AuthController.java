package com.example.answersboxapi.controller;

import com.example.answersboxapi.model.User;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    public ResponseEntity<User> signUp(@RequestBody final SignUpRequest requestUser) {
        return new ResponseEntity<>(authService.signUp(requestUser), HttpStatus.CREATED);
    }
}
