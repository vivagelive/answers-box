package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.config.JwtTokenProvider;
import com.example.answersboxapi.exceptions.EntityAlreadyProcessedException;
import com.example.answersboxapi.exceptions.EntityNotFoundException;
import com.example.answersboxapi.exceptions.UnauthorizedException;
import com.example.answersboxapi.model.User;
import com.example.answersboxapi.model.auth.SignInRequest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.service.AuthService;
import com.example.answersboxapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public User signUp(final SignUpRequest requestUser) {
        if (userService.existByEmail(requestUser.getEmail())) {
            throw new EntityAlreadyProcessedException(format("User with email %s already exist", requestUser.getEmail()));
        }
        return userService.create(requestUser);
    }

    @Override
    public TokenResponse signIn(final SignInRequest requestUser) {
        final Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(requestUser.getEmail(), requestUser.getPassword()));
        if (authentication == null) {
            throw new UnauthorizedException("Email or password is invalid");
        }
        return jwtTokenProvider.createToken(authentication);
    }

    @Override
    public void logout() {
        final User currentUser = userService.getCurrent();

        if (currentUser != null) {
            SecurityContextHolder.clearContext();
        } else throw new EntityNotFoundException("User not found");
    }
}
