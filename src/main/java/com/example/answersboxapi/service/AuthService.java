package com.example.answersboxapi.service;

import com.example.answersboxapi.model.user.User;
import com.example.answersboxapi.model.auth.SignInRequest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;

public interface AuthService {

    User signUp(final SignUpRequest signUpRequest);

    TokenResponse signIn(final SignInRequest signInRequest);

    void logout();
}
