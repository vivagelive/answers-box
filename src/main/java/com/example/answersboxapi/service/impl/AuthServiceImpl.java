package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.exceptions.EntityAlreadyProcessedException;
import com.example.answersboxapi.model.User;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.service.AuthService;
import com.example.answersboxapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    @Override
    public User signUp(final SignUpRequest requestUser) {
        if (userService.existByEmail(requestUser.getEmail())) {
            throw new EntityAlreadyProcessedException(format("User with email %s already exist", requestUser.getEmail()));
        }
        return userService.create(requestUser);
    }
}
