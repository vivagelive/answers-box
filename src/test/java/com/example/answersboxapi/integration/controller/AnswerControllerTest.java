package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.answer.AnswerRequest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static com.example.answersboxapi.utils.GeneratorUtil.*;
import static com.example.answersboxapi.utils.assertions.AssertionsCaseForModel.assertAnswerFieldsEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnswerControllerTest extends AbstractIntegrationTest {

    @Test
    public void create_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User savedUser = createUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final QuestionRequest questionRequest = generateQuestionRequest();
        Question savedQuestion = createQuestion(token, questionRequest);

        final AnswerRequest answerRequest = generateAnswerRequest();
        answerRequest.setQuestionId(savedQuestion.getId());

        //when
        final MvcResult result = mockMvc.perform(post(ANSWER_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        final Answer createdAnswer = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Answer.class);

        //then
        assertAnswerFieldsEquals(createdAnswer, savedUser, savedQuestion);
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
        result.andExpect(status().isUnauthorized());
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

    @Test
    public void create_whenQuestionDoesntExist() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        createUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final AnswerRequest answerRequest = generateAnswerRequest();
        answerRequest.setQuestionId(UUID.randomUUID());

        //when
        final ResultActions result = mockMvc.perform(post(ANSWER_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)));

        //then
        result.andExpect(status().isNotFound());
    }
}
