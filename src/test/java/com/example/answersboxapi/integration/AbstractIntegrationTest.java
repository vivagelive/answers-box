package com.example.answersboxapi.integration;

import com.example.answersboxapi.entity.UserEntity;
import com.example.answersboxapi.model.User;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.repository.UserRepository;
import com.example.answersboxapi.utils.PostgresInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.answersboxapi.mapper.UserMapper.USER_MAPPER;
import static com.example.answersboxapi.utils.GeneratorUtil.generateUser;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = PostgresInitializer.class)
public class AbstractIntegrationTest {

    protected static final String AUTH_URL = "/api/v1/auth";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected UserRepository userRepository;

    @AfterEach
    protected void clearDataBase() {
        userRepository.deleteAll();
    }

    protected User createUserSignUp(final SignUpRequest user) {
        final String encodedPassword = passwordEncoder.encode(user.getPassword());

        final UserEntity userToSave = generateUser();
        userToSave.setEmail(user.getEmail());
        userToSave.setPassword(encodedPassword);

        return USER_MAPPER.toModel(userRepository.saveAndFlush(userToSave));
    }
}
