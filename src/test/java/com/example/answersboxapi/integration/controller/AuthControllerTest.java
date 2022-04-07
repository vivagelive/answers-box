package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.auth.SignInRequest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.user.User;
import com.example.answersboxapi.utils.assertions.AssertionsCaseForModel;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.answersboxapi.enums.UserEntityRole.ROLE_USER;
import static com.example.answersboxapi.utils.GeneratorUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest extends AbstractIntegrationTest {

    @Test
    public void signUp_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();

        //when
        final MvcResult result = mockMvc.perform(post(AUTH_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                        .andExpect(status().isCreated())
                        .andReturn();

        final User response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), User.class);

        //then
        assertAll(
                () -> AssertionsCaseForModel.assertUsersFieldsNotNull(response),
                () -> AssertionsCaseForModel.assertUsersFieldsEquals(signUpRequest, response)
        );
    }

    @Test
    public void signUp_whenEmailExist() throws Exception {
        //given
        final User savedUser = insertUser();

        final SignUpRequest signUpRequest = generateSignUpRequest(savedUser.getEmail(), savedUser.getPassword());

        //when
        final ResultActions resultAction = mockMvc.perform(post(AUTH_URL + "/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        //then
        resultAction.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void signIn_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUserOrAdmin(signUpRequest, ROLE_USER);

        //when
        final TokenResponse token = createSignIn(signUpRequest);

        //then
        assertNotNull(token);
    }

    @Test
    public void signIn_whenEmailInvalid() throws Exception {
        //given
        final User savedUser = insertUser();

        final SignInRequest signInRequest = generateInvalidSignInRequest();
        signInRequest.setPassword(savedUser.getPassword());

        //when
        final ResultActions result = mockMvc.perform(post(AUTH_URL + "/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)));

        //then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void signIn_wrongUser() throws Exception {
        //given
        final SignInRequest signInRequest =
                generateSignInRequest(generateSignUpRequest().getEmail(), generateSignUpRequest().getPassword());

        //when
        final ResultActions result = mockMvc.perform(post(AUTH_URL + "/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signInRequest)));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void logout_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUserOrAdmin(signUpRequest, ROLE_USER);

        final TokenResponse token = createSignIn(signUpRequest);
        final TokenRequest tokenRequest = generateTokenRequest(token.getAccessToken());

        //when
        mockMvc.perform(post(AUTH_URL + "/logout")
                        .header(AUTHORIZATION,TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRequest)))
                        .andExpect(status().isOk())
                        .andReturn();

        //then
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void logout_whenNotSignedIn() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUserOrAdmin(signUpRequest, ROLE_USER);

        final TokenResponse token = createSignIn(signUpRequest);
        final TokenRequest tokenRequest = generateTokenRequest(token.getAccessToken());

        //when
        final ResultActions result = mockMvc.perform(post(AUTH_URL + "/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tokenRequest)));

        //then
        result.andExpect(status().isUnauthorized());
    }
}
