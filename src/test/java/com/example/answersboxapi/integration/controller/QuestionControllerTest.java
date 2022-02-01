package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.user.User;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

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

        final Tag savedTag = saveTag();

        final QuestionRequest questionRequest = generateQuestionRequest();
        questionRequest.setTagId(savedTag.getId());

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

        final Tag savedTag = saveTag();

        final QuestionRequest questionRequest = generateQuestionRequest();
        questionRequest.setTagId(savedTag.getId());

        final Question savedQuestion = createQuestion(token, questionRequest);
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

        final Tag savedTag = saveTag();

        final QuestionRequest questionRequest = generateQuestionRequest();
        questionRequest.setTagId(savedTag.getId());

        final Question savedQuestion = createQuestion(usersToken, questionRequest);
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

        final Tag savedTag = saveTag();

        final QuestionRequest questionRequest = generateQuestionRequest();
        questionRequest.setTagId(savedTag.getId());

        final Question savedQuestion = createQuestion(token, questionRequest);

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

        final SignUpRequest signUpAdminRequest = generateSignUpRequest();
        insertAdmin(signUpAdminRequest);
        final TokenResponse adminsToken = createSignIn(signUpAdminRequest);

        final Tag savedTag = createTag(adminsToken, generateTagRequest());

        final QuestionRequest questionRequest = generateQuestionRequest();
        questionRequest.setTagId(savedTag.getId());

        final Question savedQuestion = createQuestion(usersToken, questionRequest);

        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), usersToken);
        insertDeletedAnswer(generateAnswerRequest(), savedUser, savedQuestion);

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

    @Test
    public void getAllFilteredByTagId_happyPath() throws Exception {
        //given
        final SignUpRequest signUpUserRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpUserRequest);
        final TokenResponse usersToken = createSignIn(signUpUserRequest);

        final Tag mainTag = saveTag();
        final Tag secondaryTag = saveTag();

        final QuestionRequest questionRequest = generateQuestionRequest();
        questionRequest.setTagId(mainTag.getId());

        final QuestionRequest secondaryRequest = generateQuestionRequest();
        secondaryRequest.setTagId(secondaryTag.getId());

        final Question savedQuestion = createQuestion(usersToken, questionRequest);
        createQuestion(usersToken, secondaryRequest);

        //when
        final MvcResult result = mockMvc.perform(get(QUESTION_URL + "/filter/{tagId}/tag", mainTag.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + usersToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final List<Question> filteredQuestions =
                objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>() {});

        //then
        assertAll(
                () -> assertEquals(1, filteredQuestions.size()),
                () -> assertQuestionsListFields(filteredQuestions, savedUser, savedQuestion)
        );
    }

    @Test
    public void getAllFilteredByTagId_whenTagNotExist() throws Exception {
        //given
        final SignUpRequest signUpUserRequest = generateSignUpRequest();
        insertUser(signUpUserRequest);
        final TokenResponse usersToken = createSignIn(signUpUserRequest);

        final Tag savedTag = saveTag();

        final QuestionRequest questionRequest = generateQuestionRequest();
        questionRequest.setTagId(savedTag.getId());
        createQuestion(usersToken, questionRequest);

        final UUID notExistingTagId = UUID.randomUUID();

        //when
        final ResultActions result = mockMvc.perform(get(QUESTION_URL +  "/filter/{tagId}/tag", notExistingTagId)
                .header(AUTHORIZATION, TOKEN_PREFIX + usersToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNotFound());
    }

}
