package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.enums.UserEntityRole;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.UUID;
import java.util.stream.Stream;

import static com.example.answersboxapi.enums.UserEntityRole.*;
import static com.example.answersboxapi.enums.UserEntityRole.ROLE_ADMIN;
import static com.example.answersboxapi.enums.UserEntityRole.ROLE_USER;
import static com.example.answersboxapi.utils.GeneratorUtil.*;
import static com.example.answersboxapi.utils.assertions.AssertionsCaseForModel.assertAnswerFieldsEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnswerControllerTest extends AbstractIntegrationTest {

    @ParameterizedTest
    @MethodSource("createWithStatusesAndRoles")
    public void create_withUserAndAdminsAccessOrEmptyAnswer(final ResultMatcher status, final UserEntityRole role, final AnswerRequest answerRequest) throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final QuestionRequest questionRequest = generateQuestionRequest();
        Question savedQuestion = createQuestion(token, questionRequest);

        answerRequest.setQuestionId(savedQuestion.getId());

        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        final TokenResponse activeToken = createSignIn(activeUserRequest);

        //when
        final ResultActions result = mockMvc.perform(post(ANSWER_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)));

        //then
        result.andExpect(status);

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
    public void create_whenQuestionDoesntExist() throws Exception { //todo
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

    @ParameterizedTest
    @MethodSource("updateWithStatusesAndRoles")
    public void update_whenUserNotCreatorOfAnswerOrNotFoundOrAdminAccess(final ResultMatcher status, UUID id,
                                                                         final UserEntityRole role) throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse answerCreatorToken = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(answerCreatorToken, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), answerCreatorToken);

        final AnswerUpdateRequest updateAnswerRequest = generateAnswerUpdateRequest();

        final SignUpRequest usersRequest = generateSignUpRequest();
        insertUserOrAdmin(usersRequest, role);
        final TokenResponse activeToken = createSignIn(usersRequest);

        if (id == null) {
             id = savedAnswer.getId();
        }

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}", id)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAnswerRequest)));

        //then
        result.andExpect(status);
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

    @ParameterizedTest
    @MethodSource("deleteWithStatusesRolesAndIds")
    public void delete_whenUserNotCreatorOfAnswerOrWithAdminAccess(final ResultMatcher status, final UserEntityRole role,
                                                                   UUID id, final SignUpRequest activeUserRequest) throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        insertUserOrAdmin(activeUserRequest, role);
        final TokenResponse activeToken = createSignIn(activeUserRequest);

        if (id == null) {
            id = savedAnswer.getId();
        }

        //when
        final ResultActions result = mockMvc.perform(delete(ANSWER_URL + "/{id}", id)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status);
    }

    @ParameterizedTest
    @MethodSource("increaseRating")
    public void increaseRatingById_withUserAndAdminAccessOrNotFound(final ResultMatcher status, UUID id, final UserEntityRole role,
                                             final Integer increaseDelta) throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        final TokenResponse activeToken = createSignIn(activeUserRequest);

        if (id == null) {
            id = savedAnswer.getId();
        }

        //when
        mockMvc.perform(put(ANSWER_URL + "/{id}/increase-rating", id)
                        .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status)
                        .andReturn();

        final AnswerEntity foundAnswer = answerRepository.getById(savedAnswer.getId());

        //then
        assertEquals(savedAnswer.getRating() + increaseDelta, foundAnswer.getRating());
    }

    @Test
    public void increaseRatingById_whenNotSignedIn() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}/increase-rating", savedAnswer.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("decreaseRating")
    public void decreaseRatingById_withUserAndAdminsAccessOrNotFound(final ResultMatcher status, UUID id, final UserEntityRole role,
                                                                  final Integer decreaseDelta) throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);

        final TokenResponse activeToken = createSignIn(activeUserRequest);

        if (id == null) {
            id = savedAnswer.getId();
        }

        //when
        mockMvc.perform(put(ANSWER_URL + "/{id}/decrease-rating", id)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andReturn();

        final AnswerEntity foundAnswer = answerRepository.getById(savedAnswer.getId());

        //then
        assertEquals(savedAnswer.getRating() + decreaseDelta, foundAnswer.getRating());
    }

    @Test
    public void decreaseRatingById_whenNotSignedIn() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}/decrease-rating", savedAnswer.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    static Stream<Arguments> createWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isCreated(), ROLE_USER, generateAnswerRequest()),
                arguments(status().isForbidden(), ROLE_ADMIN, generateAnswerRequest()),
                arguments(status().isBadRequest(), ROLE_USER, generateEmptyAnswer()));
    }

    static Stream<Arguments> updateWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isOk(), null, ROLE_ADMIN),
                arguments(status().isNotFound(), UUID.randomUUID(), ROLE_USER),
                arguments(status().isForbidden(), null, ROLE_USER));
    }

    static Stream<Arguments> deleteWithStatusesRolesAndIds() {
        return Stream.of(
                arguments(status().isForbidden(), ROLE_USER, null, generateSignUpRequest()),
                arguments(status().isNoContent(), ROLE_ADMIN, null, generateSignUpRequest()),
                arguments(status().isNotFound(), ROLE_USER, UUID.randomUUID(), generateSignUpRequest()));
    }
}
