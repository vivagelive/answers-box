package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.answer.AnswerRequest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.answersboxapi.utils.GeneratorUtil.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnswerController extends AbstractIntegrationTest {

    @Test
    public void create_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        createUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final AnswerRequest answerRequest = generateAnswerRequest();

        //when
        final ResultActions result = mockMvc.perform(post(ANSWER_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)));

        //then
        result.andExpect(status().isCreated());
    }

    @Test
    public void create_whenForbidden() throws Exception {
        //given
        createUser(generateSignUpRequest());

        final AnswerRequest answerRequest = generateAnswerRequest();

        //when
        final ResultActions result = mockMvc.perform(post(ANSWER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    public void create_withAdminAccess() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        createAdmin(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final AnswerRequest answerRequest = generateAnswerRequest();

        //when
        final ResultActions result = mockMvc.perform(post(ANSWER_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    public void create_withEmptyAnswer() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        createUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final AnswerRequest answerRequest = generateEmptyAnswer();

        //when
        final ResultActions result = mockMvc.perform(post(ANSWER_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)));

        //then
        result.andExpect(status().isBadRequest());
    }
}
