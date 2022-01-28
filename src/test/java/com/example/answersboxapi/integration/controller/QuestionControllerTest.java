package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.user.User;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static com.example.answersboxapi.utils.GeneratorUtil.*;
import static com.example.answersboxapi.utils.assertions.AssertionsCaseForModel.assertAnswersListFields;
import static com.example.answersboxapi.utils.assertions.AssertionsCaseForModel.assertQuestionsListFields;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class QuestionControllerTest extends AbstractIntegrationTest {

    @Test
    public void create_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

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
        insertAdmin(signUpRequest);

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
        insertUser(signUpRequest);

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

    @Test
    public void getAll_withUserAccess() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        insertDeletedQuestion(generateQuestionRequest(), savedUser);

        //when
        final ResultActions result = mockMvc.perform(get(QUESTION_URL + "/all")
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        final List<Question> foundQuestions =
                objectMapper.readValue(result.andReturn().getResponse().getContentAsByteArray(), new TypeReference<>() {});

        //then
        assertAll(
                () -> assertEquals(1, foundQuestions.size()),
                () -> assertQuestionsListFields(foundQuestions, savedUser, savedQuestion)
        );
    }

    @Test
    public void getAll_withAdminAccess() throws Exception {
        //given
        final SignUpRequest signUpUserRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpUserRequest);
        final TokenResponse usersToken = createSignIn(signUpUserRequest);


        final SignUpRequest signUpAdminRequest = generateSignUpRequest();
        insertAdmin(signUpAdminRequest);
        final TokenResponse adminsToken = createSignIn(signUpAdminRequest);

        final Question savedQuestion = createQuestion(usersToken, generateQuestionRequest());
        insertDeletedQuestion(generateQuestionRequest(), savedUser);

        //when
        final ResultActions result = mockMvc.perform(get(QUESTION_URL + "/all")
                        .header(AUTHORIZATION, TOKEN_PREFIX + adminsToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        final List<Question> foundQuestions =
                objectMapper.readValue(result.andReturn().getResponse().getContentAsByteArray(), new TypeReference<>() {});

        //then
        assertAll(
                () -> assertEquals(2, foundQuestions.size()),
                () -> assertQuestionsListFields(foundQuestions, savedUser, savedQuestion)
        );
    }

    @Test
    public void getAnswersByQuestionId_withUserAccess() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);
        insertDeletedAnswer(generateAnswerRequest(), savedUser, savedQuestion);

        //when
        final MvcResult result = mockMvc.perform(get(format(QUESTION_URL + "/%s/answers", savedQuestion.getId()))
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final List<Answer> foundAnswers =
                objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>() {});

        //then
        assertAll(
                () -> assertEquals(1, foundAnswers.size()),
                () -> assertAnswersListFields(foundAnswers, savedAnswer)
        );
    }

    @Test
    public void getAnswersByQuestionId_withAdminAccess() throws Exception {
        //given
        final SignUpRequest signUpUserRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpUserRequest);
        final TokenResponse usersToken = createSignIn(signUpUserRequest);

        final Question savedQuestion = createQuestion(usersToken, generateQuestionRequest());

        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), usersToken);
        insertDeletedAnswer(generateAnswerRequest(), savedUser, savedQuestion);

        final SignUpRequest signUpAdminRequest = generateSignUpRequest();
        insertAdmin(signUpAdminRequest);
        final TokenResponse adminsToken = createSignIn(signUpAdminRequest);

        //when
        final MvcResult result = mockMvc.perform(get(format(QUESTION_URL + "/%s/answers", savedQuestion.getId()))
                        .header(AUTHORIZATION, TOKEN_PREFIX + adminsToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final List<Answer> foundAnswers =
                objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>() {});

        //then
        assertAll(
                () -> assertEquals(2, foundAnswers.size()),
                () -> assertAnswersListFields(foundAnswers, savedAnswer)
        );
    }
}
