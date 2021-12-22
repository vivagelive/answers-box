package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.entity.UserEntity;
import com.example.answersboxapi.enums.UserEntityRole;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.User;
import com.example.answersboxapi.repository.UserRepository;
import com.example.answersboxapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.example.answersboxapi.mapper.UserMapper.USER_MAPPER;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User create(final SignUpRequest requestUser) {

        final UserEntity userToSave = UserEntity.builder()
                .firstName(requestUser.getFirstName())
                .lastName(requestUser.getLastName())
                .email(requestUser.getEmail())
                .password(passwordEncoder.encode(requestUser.getPassword()))
                .createdAt(Instant.now())
                .role(UserEntityRole.ROLE_USER)
                .build();

        return USER_MAPPER.toModel(userRepository.saveAndFlush(userToSave));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existByEmail(final String email) {
        return userRepository.existsByEmail(email);
    }
}
