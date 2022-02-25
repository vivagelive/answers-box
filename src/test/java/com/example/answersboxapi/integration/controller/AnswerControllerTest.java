package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.answer.AnswerRequest;
import com.example.answersboxapi.model.answer.AnswerUpdateRequest;
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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnswerControllerTest extends AbstractIntegrationTest {

    @Test
    public void create_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpRequest);

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
    public void create_whenNotSignedIn() throws Exception {
        //given
        insertUser(generateSignUpRequest());

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
        insertAdmin(signUpRequest);

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
        insertUser(signUpRequest);

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
        insertUser(signUpRequest);

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

    @Test
    public void update_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        final AnswerUpdateRequest updateAnswerRequest = generateAnswerUpdateRequest();

        //when
        final MvcResult result = mockMvc.perform(put(ANSWER_URL + "/{id}", savedAnswer.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAnswerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        final Answer updatedAnswer = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Answer.class);

        //then
        assertAll(
                () -> assertEquals(updateAnswerRequest.getText(), updatedAnswer.getText()),
                () -> assertAnswerFieldsEquals(savedAnswer, savedUser, savedQuestion)
        );
    }

    @Test
    public void update_whenUserNotCreatorOfAnswer() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse answerCreatorToken = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(answerCreatorToken, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), answerCreatorToken);

        final AnswerUpdateRequest updateAnswerRequest = generateAnswerUpdateRequest();

        final SignUpRequest usersRequest = generateSignUpRequest();
        insertUser(usersRequest);
        final TokenResponse usersToken = createSignIn(usersRequest);

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}", savedAnswer.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + usersToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAnswerRequest)));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    public void update_withAdminAccess() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpRequest);

        final TokenResponse usersToken = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(usersToken, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), usersToken);

        final AnswerUpdateRequest updateAnswerRequest = generateAnswerUpdateRequest();

        final SignUpRequest adminsRequest = generateSignUpRequest();
        insertAdmin(adminsRequest);

        final TokenResponse adminsToken = createSignIn(adminsRequest);

        //when
        final MvcResult result = mockMvc.perform(put(ANSWER_URL + "/{id}", savedAnswer.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + adminsToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAnswerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        final Answer updatedAnswer = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Answer.class);

        //then

        assertAll(
                () -> assertEquals(updateAnswerRequest.getText(), updatedAnswer.getText()),
                () -> assertAnswerFieldsEquals(savedAnswer, savedUser, savedQuestion)
        );
    }

    @Test
    public void update_whenAnswerNotFound() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final AnswerUpdateRequest updateAnswerRequest = generateAnswerUpdateRequest();

        createQuestion(token, generateQuestionRequest());

        final UUID notExistingId = UUID.randomUUID();

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}", notExistingId)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAnswerRequest)));

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    public void update_whenNotSignedIn() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse usersToken = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(usersToken, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), usersToken);

        final AnswerUpdateRequest updateAnswerRequest = generateAnswerUpdateRequest();

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}", savedAnswer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAnswerRequest)));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteById_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        //when
        final ResultActions result = mockMvc.perform(delete(ANSWER_URL + "/{id}", savedAnswer.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNoContent());
    }

    @Test
    public void delete_whenNotSignedIn() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        //when
        final ResultActions result = mockMvc.perform(delete(ANSWER_URL + "/{id}", savedAnswer.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void delete_whenAnswerNotFound() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        final UUID notExistingId = UUID.randomUUID();

        //when
        final ResultActions result = mockMvc.perform(delete(ANSWER_URL + "/{id}", notExistingId)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    public void delete_whenUserNotCreatorOfAnswer() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse answerCreatorToken = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(answerCreatorToken, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), answerCreatorToken);

        final SignUpRequest userRequest = generateSignUpRequest();
        insertUser(userRequest);

        final TokenResponse userToken = createSignIn(userRequest);

        //when
        final ResultActions result = mockMvc.perform(delete(ANSWER_URL + "/{id}", savedAnswer.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + userToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    public void delete_withAdminAccess() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        final SignUpRequest adminsRequest = generateSignUpRequest();
        insertAdmin(adminsRequest);

        final TokenResponse adminsToken = createSignIn(adminsRequest);

        //when
        final ResultActions result = mockMvc.perform(delete(ANSWER_URL + "/{id}", savedAnswer.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + adminsToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNoContent());
    }

    @Test
    public void increaseRating_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        //when
        mockMvc.perform(put(ANSWER_URL + "/{id}/increase", savedAnswer.getId())
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        final AnswerEntity foundAnswer = answerRepository.getById(savedAnswer.getId());

        //then
        assertEquals(1, foundAnswer.getRating());
    }

    @Test
    public void increaseRating_whenNotSignedIn() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}/increase", savedAnswer.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void increase_whenAnswerNotFound() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        createQuestion(token, generateQuestionRequest());

        final UUID notExistingId = UUID.randomUUID();

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}/increase", notExistingId)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    public void increase_withAdminAccess() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        final SignUpRequest adminRequest = generateSignUpRequest();
        insertAdmin(adminRequest);

        final TokenResponse adminsToken = createSignIn(adminRequest);

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}/increase", savedAnswer.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + adminsToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    public void decrease_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        //when
        mockMvc.perform(put(ANSWER_URL + "/{id}/decrease", savedAnswer.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final AnswerEntity foundAnswer = answerRepository.getById(savedAnswer.getId());

        //then
        assertEquals(-1, foundAnswer.getRating());
    }

    @Test
    public void decrease_whenNotSignedIn() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}/decrease", savedAnswer.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void decrease_whenAnswerNotFound() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        final UUID notExistingId = UUID.randomUUID();

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}/decrease", notExistingId)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    public void decrease_withAdminAccess() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        final SignUpRequest adminsRequest = generateSignUpRequest();
        insertAdmin(adminsRequest);

        final TokenResponse adminsToken = createSignIn(adminsRequest);

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}/decrease", savedAnswer.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + adminsToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isForbidden());
    }
}
