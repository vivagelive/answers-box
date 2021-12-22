package com.example.answersboxapi.service;

import com.example.answersboxapi.model.User;
import com.example.answersboxapi.model.auth.SignUpRequest;

public interface AuthService {

    User signUp(final SignUpRequest signUpRequest);
}
