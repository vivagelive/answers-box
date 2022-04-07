package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.enums.UserEntityRole;
import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.answer.AnswerRequest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;

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
    public void createAnswer(final ResultMatcher status, final UserEntityRole role, final AnswerRequest answerRequest,
                             final boolean happyCondition) throws Exception {
        //given
        answerRequest.setQuestionId(savedQuestion.getId());

        final SignUpRequest activeUserRequest = generateSignUpRequest();
        final User activeUser = insertUserOrAdmin(activeUserRequest, role);
        final TokenResponse activeToken = createSignIn(activeUserRequest);

        //when
        final MvcResult result = mockMvc.perform(post(ANSWER_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)))
                .andExpect(status)
                .andReturn();

        //then
        assertCondition(happyCondition, result, activeUser);
    }

    @ParameterizedTest
    @MethodSource("httpMethodsWithUrls")
    public void answerEndpoints_whenNotSignedIn(final HttpMethod method, final String url) throws Exception {
        //given & when
        final ResultActions result = mockMvc.perform(request(method, ANSWER_URL + url, savedAnswer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void createAnswer_whenQuestionDoesntExist() throws Exception {
        answerRequest.setQuestionId(UUID.randomUUID());

        //when
        final ResultActions result = mockMvc.perform(post(ANSWER_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)));

        //then
        result.andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource("updateWithStatusesAndRoles")
    public void updateAnswer(final ResultMatcher status, UUID id, final UserEntityRole role, final boolean isCreator,
                             final boolean happyCondition) throws Exception {
        //given
        final SignUpRequest usersRequest = generateSignUpRequest();
        insertUserOrAdmin(usersRequest, role);
        TokenResponse activeToken = createSignIn(usersRequest);

        final UUID idForSearch = checkIdForSearch(id, savedAnswer.getId());

        activeToken = isCreator(isCreator, activeToken, token);

        //when
        final MvcResult result = mockMvc.perform(put(ANSWER_URL + "/{id}", idForSearch)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAnswerRequest)))
                .andExpect(status)
                .andReturn();

        //then
        assertCondition(happyCondition, result, savedUser);
    }

    @ParameterizedTest
    @MethodSource("deleteWithStatusesRolesAndIds")
    public void deleteAnswer(final ResultMatcher status, final UserEntityRole role, UUID id, final boolean isCreator) throws Exception {
        //given
        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        TokenResponse activeToken = createSignIn(activeUserRequest);

        final UUID idForSearch = checkIdForSearch(id, savedAnswer.getId());

        activeToken = isCreator(isCreator, activeToken, token);

        //when
        final ResultActions result = mockMvc.perform(delete(ANSWER_URL + "/{id}", idForSearch)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status);
    }

    @ParameterizedTest
    @MethodSource("increaseRating")
    public void increaseAnswerRating(final ResultMatcher status, UUID id, final UserEntityRole role,
                                     final Integer increaseDelta, final boolean happyCondition) throws Exception {
        //given
        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        final TokenResponse activeToken = createSignIn(activeUserRequest);

        final UUID idForSearch = checkIdForSearch(id, savedAnswer.getId());

        //when
        final MvcResult result = mockMvc.perform(put(ANSWER_URL + "/{id}/increase-rating", idForSearch)
                        .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status)
                        .andReturn();

        final AnswerEntity foundAnswer = answerRepository.getById(savedAnswer.getId());

        //then
        assertAll(
                () -> assertEquals(savedAnswer.getRating() + increaseDelta, foundAnswer.getRating()),
                () -> assertCondition(happyCondition, result, savedUser));
    }

    @ParameterizedTest
    @MethodSource("decreaseRating")
    public void decreaseAnswerRating(final ResultMatcher status, UUID id, final UserEntityRole role,
                                     final Integer decreaseDelta, final boolean happyCondition) throws Exception {
        //given
        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        final TokenResponse activeToken = createSignIn(activeUserRequest);

        final UUID idForSearch = checkIdForSearch(id, savedAnswer.getId());

        //when
        final MvcResult result = mockMvc.perform(put(ANSWER_URL + "/{id}/decrease-rating", idForSearch)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andReturn();

        final AnswerEntity foundAnswer = answerRepository.getById(savedAnswer.getId());

        //then
        assertAll(
                () -> assertEquals(savedAnswer.getRating() + decreaseDelta, foundAnswer.getRating()),
                () -> assertCondition(happyCondition, result, savedUser));
    }

    static Stream<Arguments> createWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isCreated(), ROLE_USER, generateAnswerRequest(), true),
                arguments(status().isForbidden(), ROLE_ADMIN, generateAnswerRequest(), false),
                arguments(status().isBadRequest(), ROLE_USER, generateEmptyAnswer(), false));
    }

    static Stream<Arguments> updateWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isOk(), null, ROLE_USER, true, true),
                arguments(status().isOk(), null, ROLE_ADMIN, false, true),
                arguments(status().isNotFound(), UUID.randomUUID(), ROLE_USER, false, false),
                arguments(status().isForbidden(), null, ROLE_USER, false, false));
    }

    static Stream<Arguments> deleteWithStatusesRolesAndIds() {
        return Stream.of(
                arguments(status().isNoContent(), ROLE_USER, null, true),
                arguments(status().isForbidden(), ROLE_USER, null, false),
                arguments(status().isNoContent(), ROLE_ADMIN, null, false),
                arguments(status().isNotFound(), ROLE_USER, UUID.randomUUID(), false));
    }

    static Stream<Arguments> httpMethodsWithUrls() {
        return Stream.of(
                arguments(HttpMethod.POST, ""),
                arguments(HttpMethod.PUT, "/{id}"),
                arguments(HttpMethod.DELETE, "/{id}"),
                arguments(HttpMethod.PUT, "/{id}/increase-rating"),
                arguments(HttpMethod.PUT, "/{id}/decrease-rating"));
    }

    private void assertCondition(final boolean condition, final MvcResult result, final User activeUser) throws IOException {
        if (condition){
            final Answer foundAnswer = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Answer.class);

            assertAnswerFieldsEquals(foundAnswer, activeUser, savedQuestion);
        }
    }
}
