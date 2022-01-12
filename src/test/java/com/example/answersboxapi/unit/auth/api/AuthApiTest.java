package com.example.answersboxapi.unit.auth.api;

import com.example.answersboxapi.exceptions.UnexpectedException;
import com.example.answersboxapi.model.User;
import com.example.answersboxapi.model.auth.SignInRequest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.service.AuthService;
import com.example.answersboxapi.unit.AbstractUnitTest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.example.answersboxapi.utils.GeneratorUtil.*;
import static com.example.answersboxapi.utils.constants.Messages.INVALID_EMAIL_MESSAGE;
import static com.example.answersboxapi.utils.constants.Messages.INVALID_PASSWORD_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthApiTest extends AbstractUnitTest {

    private static final String AUTH_URL = "/api/v1/auth";

    @Mock
    private AuthService authService;

    @Test
    public void signUp_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        when(authService.signUp(signUpRequest)).thenReturn(new User());

        //when
        final ResultActions result = mockMvc.perform(post(AUTH_URL + "/sign-up")
                .content(objectMapper.writeValueAsString(signUpRequest))
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isCreated());
    }

    @Test
    public void signUp_whenEmailInvalid() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest(INVALID_EMAIL, VALID_PASSWORD);

        //when
        final MvcResult result = mockMvc.perform(post(AUTH_URL + "/sign-up")
                        .content(objectMapper.writeValueAsString(signUpRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        final List<UnexpectedException> errors = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), new TypeReference<>() {
        });

        //then
        assertAll(
                () -> assertEquals(1, errors.size()),
                () -> assertEquals(INVALID_EMAIL_MESSAGE, errors.get(0).getMessage())
        );
    }

    @Test
    public void signUp_whenPasswordInvalid() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest(VALID_EMAIL, INVALID_PASSWORD);

        //when
        final MvcResult result = mockMvc.perform(post(AUTH_URL + "/sign-up")
                        .content(objectMapper.writeValueAsString(signUpRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        final List<UnexpectedException> errors = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), new TypeReference<>() {
        });

        //then
        assertAll(
                () -> assertEquals(1, errors.size()),
                () -> assertEquals(INVALID_PASSWORD_MESSAGE, errors.get(0).getMessage())
        );
    }

    @Test
    public void signIn_happyPath() throws Exception {
        //given
        final SignInRequest signInRequest = generateSignInRequest(VALID_EMAIL, VALID_PASSWORD);
        when(authService.signIn(signInRequest)).thenReturn(new TokenResponse());

        //when
        final ResultActions result = mockMvc.perform(post(AUTH_URL + "/sign-in")
                .content(objectMapper.writeValueAsString(signInRequest))
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isCreated());
    }

    @Test
    public void signIn_whenEmailInvalid() throws Exception {
        //given
        final SignInRequest signInRequest = generateInvalidSignInRequest(INVALID_EMAIL, VALID_PASSWORD);

        //when
        final MvcResult result = mockMvc.perform(post(AUTH_URL + "/sign-in")
                        .content(objectMapper.writeValueAsString(signInRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        final List<UnexpectedException> errors = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), new TypeReference<>() {
        });

        //then
        assertAll(
                () -> assertEquals(1, errors.size()),
                () -> assertEquals(INVALID_EMAIL_MESSAGE, errors.get(0).getMessage())
        );
    }

    @Test
    public void signIn_whenPasswordInvalid() throws Exception {
        //given
        final SignInRequest signInRequest = generateInvalidSignInRequest(VALID_EMAIL, INVALID_PASSWORD);

        //when
        final MvcResult result = mockMvc.perform(post(AUTH_URL + "/sign-in")
                        .content(objectMapper.writeValueAsString(signInRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        final List<UnexpectedException> errors = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), new TypeReference<>() {
        });

        //then
        assertAll(
                () -> assertEquals(1, errors.size()),
                () -> assertEquals(INVALID_PASSWORD_MESSAGE, errors.get(0).getMessage())
        );
    }

    @Test
    public void signIn_whenMultipleInvalidFields() throws Exception {
        //given
        final SignInRequest signInRequest = generateInvalidSignInRequest(INVALID_EMAIL, INVALID_PASSWORD);

        //when
        final MvcResult result = mockMvc.perform(post(AUTH_URL + "/sign-in")
                        .content(objectMapper.writeValueAsString(signInRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        final List<UnexpectedException> errors = objectMapper.readValue(result.getResponse()
                .getContentAsByteArray(), new TypeReference<>() {
        });

        //then
        assertEquals(2, errors.size());
    }
}
