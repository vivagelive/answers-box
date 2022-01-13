package com.example.answersboxapi.integration;

import com.example.answersboxapi.entity.UserEntity;
import com.example.answersboxapi.enums.UserEntityRole;
import com.example.answersboxapi.model.User;
import com.example.answersboxapi.model.auth.SignInRequest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.repository.UserRepository;
import com.example.answersboxapi.utils.PostgresInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.example.answersboxapi.mapper.UserMapper.USER_MAPPER;
import static com.example.answersboxapi.utils.GeneratorUtil.generateSignInRequest;
import static com.example.answersboxapi.utils.GeneratorUtil.generateUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = PostgresInitializer.class)
public class AbstractIntegrationTest {

    protected static final String AUTHORIZATION = "Authorization";
    protected static final String TOKEN_PREFIX = "Bearer ";

    protected static final String AUTH_URL = "/api/v1/auth";
    protected static final String USER_URL = "/api/v1/users";

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

    protected User createUser() {
        return USER_MAPPER.toModel(userRepository.saveAndFlush(generateUser()));
    }

    protected TokenResponse createSignIn(final SignUpRequest signedUpUser) throws Exception{
        final SignInRequest signInRequest = generateSignInRequest(signedUpUser.getEmail(), signedUpUser.getPassword());

        final MvcResult result = mockMvc.perform(post(AUTH_URL + "/sign-in")
                .content(objectMapper.writeValueAsString(signInRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsByteArray(),TokenResponse.class);
    }

    protected User createUser(final SignUpRequest signUpRequest) {
        final UserEntity userToSave = createEntity(signUpRequest);

        return USER_MAPPER.toModel(userRepository.saveAndFlush(userToSave));
    }

    protected User createAdmin(final SignUpRequest signUpRequest) {
        final UserEntity userToSave = createEntity(signUpRequest);
        userToSave.setRole(UserEntityRole.ROLE_ADMIN);

        return USER_MAPPER.toModel(userRepository.saveAndFlush(userToSave));
    }

    private UserEntity createEntity(final SignUpRequest signUpRequest){
        final String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());

        final UserEntity userToSave = generateUser();
        userToSave.setEmail(signUpRequest.getEmail());
        userToSave.setPassword(encodedPassword);

        return userToSave;
    }
}
