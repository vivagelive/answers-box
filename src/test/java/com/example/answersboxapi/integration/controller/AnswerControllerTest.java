package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.enums.UserEntityRole;
import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.answer.AnswerRequest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.UUID;
import java.util.stream.Stream;

import static com.example.answersboxapi.enums.UserEntityRole.ROLE_ADMIN;
import static com.example.answersboxapi.enums.UserEntityRole.ROLE_USER;
import static com.example.answersboxapi.utils.GeneratorUtil.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AnswerControllerTest extends AbstractIntegrationTest {

    @ParameterizedTest
    @MethodSource("createWithStatusesAndRoles")
    public void create_happyPath(final ResultMatcher status, final UserEntityRole role, final AnswerRequest answerRequest) throws Exception {
        //given
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
        //given & when
        final ResultActions result = mockMvc.perform(post(ANSWER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(answerRequest)));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void create_whenQuestionDoesntExist() throws Exception {
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
    public void update_happyPath(final ResultMatcher status, UUID id, final UserEntityRole role, final boolean isCreator) throws Exception {
        //given
        final SignUpRequest usersRequest = generateSignUpRequest();
        insertUserOrAdmin(usersRequest, role);
        TokenResponse activeToken = createSignIn(usersRequest);

        final UUID idForSearch = checkIdForSearch(id, savedAnswer.getId());

        activeToken = isCreator(isCreator, activeToken, token);

        //when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}", idForSearch)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAnswerRequest)));

        //then
        result.andExpect(status);
    }

    @Test
    public void update_whenNotSignedIn() throws Exception {
        //given & when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}", savedAnswer.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateAnswerRequest)));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void delete_whenNotSignedIn() throws Exception {
        //given & when
        final ResultActions result = mockMvc.perform(delete(ANSWER_URL + "/{id}", savedAnswer.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("deleteWithStatusesRolesAndIds")
    public void delete_happyPath(final ResultMatcher status, final UserEntityRole role, UUID id, final boolean isCreator) throws Exception {
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
    public void increaseRatingById_happyPath(final ResultMatcher status, UUID id, final UserEntityRole role,
                                             final Integer increaseDelta) throws Exception {
        //given
        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        final TokenResponse activeToken = createSignIn(activeUserRequest);

        final UUID idForSearch = checkIdForSearch(id, savedAnswer.getId());

        //when
        mockMvc.perform(put(ANSWER_URL + "/{id}/increase-rating", idForSearch)
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
        //given & when
        final ResultActions result = mockMvc.perform(put(ANSWER_URL + "/{id}/increase-rating", savedAnswer.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("decreaseRating")
    public void decreaseRatingById_happyPath(final ResultMatcher status, UUID id, final UserEntityRole role,
                                             final Integer decreaseDelta) throws Exception {
        //given
        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        final TokenResponse activeToken = createSignIn(activeUserRequest);

        final UUID idForSearch = checkIdForSearch(id, savedAnswer.getId());

        //when
        mockMvc.perform(put(ANSWER_URL + "/{id}/decrease-rating", idForSearch)
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
        //given & when
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
                arguments(status().isOk(), null, ROLE_USER, true),
                arguments(status().isOk(), null, ROLE_ADMIN, false),
                arguments(status().isNotFound(), UUID.randomUUID(), ROLE_USER, false),
                arguments(status().isForbidden(), null, ROLE_USER, false));
    }

    static Stream<Arguments> deleteWithStatusesRolesAndIds() {
        return Stream.of(
                arguments(status().isNoContent(), ROLE_USER, null, true),
                arguments(status().isForbidden(), ROLE_USER, null, false),
                arguments(status().isNoContent(), ROLE_ADMIN, null, false),
                arguments(status().isNotFound(), ROLE_USER, UUID.randomUUID(), false));
    }
}
