package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.answersboxapi.utils.GeneratorUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class QuestionControllerTest extends AbstractIntegrationTest {

    @Test
    public void create_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        createUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final QuestionRequest questionRequest = generateQuestionRequest();

        //when
        final MvcResult result = mockMvc.perform(post(QUESTION_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(questionRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        final Question savedQuestion = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Question.class);

        //then
        assertAll(
                () -> assertEquals(questionRequest.getTitle(), savedQuestion.getTitle()),
                () -> assertEquals(questionRequest.getDescription(), savedQuestion.getDescription())
        );
    }

    @Test
    public void create_withAdminAccess() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        createAdmin(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final QuestionRequest questionRequest = generateQuestionRequest();

        //when
        final ResultActions result = mockMvc.perform(post(QUESTION_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(questionRequest)));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    public void create_withEmptyFields() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        createUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final QuestionRequest emptyQuestion = generateQuestionWithEmptyFields();

        //when
        final ResultActions result = mockMvc.perform(post(QUESTION_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyQuestion)));

        //then
        result.andExpect(status().isBadRequest());
    }
}
