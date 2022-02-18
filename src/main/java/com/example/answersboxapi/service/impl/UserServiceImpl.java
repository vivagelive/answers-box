package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.entity.UserEntity;
import com.example.answersboxapi.enums.UserEntityRole;
import com.example.answersboxapi.exceptions.AccessDeniedException;
import com.example.answersboxapi.exceptions.EntityNotFoundException;
import com.example.answersboxapi.exceptions.UnauthorizedException;
import com.example.answersboxapi.model.UserDetailsImpl;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.user.User;
import com.example.answersboxapi.repository.UserRepository;
import com.example.answersboxapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static com.example.answersboxapi.mapper.UserMapper.USER_MAPPER;
import static com.example.answersboxapi.utils.SecurityUtils.getCurrentUser;
import static com.example.answersboxapi.utils.SecurityUtils.isAdmin;

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

    @Override
    public User getCurrent() {
        final UserDetailsImpl userDetails = getCurrentUser();

        return USER_MAPPER.toModel(
                userRepository.findByEmail(userDetails.getEmail()).orElseThrow(
                        () -> new EntityNotFoundException(String.format("User with email %s not found", userDetails.getEmail()))));
    }

    @Override
    public void deleteById(final UUID id) {
        if (!existById(id)) {
            throw new EntityNotFoundException(String.format("User with id: %s doesnt found", id));
        }
        checkAccess(id);
        userRepository.deleteById(id);
    }

    private void checkAccess(final UUID id) {
        final User currentUser = getCurrent();
        if (!(currentUser.getId().equals(id) || isAdmin())) {
            throw new AccessDeniedException(String.format("Low access to delete user with id: %s", id));
        }
    }

    private boolean existById(final UUID id) {
        return userRepository.existsById(id);
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        final UserEntity foundUser =
                userRepository.findByEmail(email).orElseThrow(() -> new UnauthorizedException("Email or password is invalid"));

        final UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setEmail(foundUser.getEmail());
        userDetails.setPassword(foundUser.getPassword());
        userDetails.setRole(foundUser.getRole());

        return userDetails;
    }
}
