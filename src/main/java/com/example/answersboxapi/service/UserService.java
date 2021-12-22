package com.example.answersboxapi.service;

import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.User;

public interface UserService {

    User create(final SignUpRequest requestUser);

    boolean existByEmail(final String email);
}
