package com.example.answersboxapi.service;

import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User create(final SignUpRequest requestUser);

    boolean existByEmail(final String email);
}
