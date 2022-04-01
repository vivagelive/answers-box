package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.enums.UserEntityRole;
import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.SortParams;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.question.QuestionUpdateRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.example.answersboxapi.enums.UserEntityRole.ROLE_ADMIN;
import static com.example.answersboxapi.enums.UserEntityRole.ROLE_USER;
import static com.example.answersboxapi.model.SortParams.*;
import static com.example.answersboxapi.utils.GeneratorUtil.*;
import static com.example.answersboxapi.utils.assertions.AssertionsCaseForModel.assertQuestionsListFields;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class QuestionControllerTest extends AbstractIntegrationTest {

    @ParameterizedTest
    @MethodSource("createWithStatusesAndRoles")
    public void create_happyPath(final ResultMatcher status, final UserEntityRole role,
                                 final QuestionRequest questionRequest) throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUserOrAdmin(signUpRequest, role);

        final TokenResponse token = createSignIn(signUpRequest);

        //when
        final ResultActions result = mockMvc.perform(post(QUESTION_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(questionRequest)));

        //then
        result.andExpect(status);
    }

    @ParameterizedTest
    @MethodSource("deletedFlagAndSortParams")
    public void getAll_happyPath(final String deletedFlag, final SortParams sortParams, final UserEntityRole role) throws Exception {
        //given
        final Question deletedQuestion = insertDeletedQuestion(generateQuestionRequest(), savedUser);

        insertQuestionDetails(savedQuestion, savedTag);
        insertQuestionDetails(deletedQuestion, savedTag);

        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        TokenResponse activeToken = createSignIn(activeUserRequest);

        //when
        final ResultActions result = mockMvc.perform(get(QUESTION_URL + "/all")
                        .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("tagIds", savedTag.getId().toString())
                        .param("deletedFlag", deletedFlag)
                        .param("sortParams", sortParams.toString()))
                        .andExpect(status().isOk());

        final List<Question> foundQuestions =
                objectMapper.readValue(result.andReturn().getResponse().getContentAsByteArray(), new TypeReference<>() {});

        //then
        assertAll(
                () -> assertEquals(1, foundQuestions.size()),
                () -> assertQuestionsListFields(foundQuestions, savedUser, savedQuestion),
                () -> assertNull(foundQuestions.stream().findFirst().get().getDeletedAt())
        );
    }

    @ParameterizedTest
    @MethodSource("deletedFlagAndSortParams")
    public void getAnswersByQuestionId_happyPath (final String deletedFlag, final SortParams sortParams,
                                                                final UserEntityRole role) throws Exception {
        //given
        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        TokenResponse activeToken = createSignIn(activeUserRequest);

        insertDeletedAnswer(generateAnswerRequest(), savedUser, savedQuestion);

        //when
        final MvcResult result = mockMvc.perform(get(format(QUESTION_URL + "/%s/answers", savedQuestion.getId()))
                        .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isDeleted", deletedFlag)
                        .param("sortParams", sortParams.toString()))
                        .andExpect(status().isOk())
                        .andReturn();

        final List<Answer> foundAnswers =
                objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>() {});

        //then
        assertEquals(1, foundAnswers.size());
    }

    @ParameterizedTest
    @MethodSource("addTagWithStatusesAndRoles")
    public void addTagToQuestion_happyPath(final ResultMatcher status, UUID questionId, UUID tagId) throws Exception {
        //given
        final UUID questionIdForSearch = checkIdForSearch(questionId, savedQuestion.getId());
        final UUID tagIdForSearch = checkIdForSearch(tagId, savedTag.getId());

        //when
        final ResultActions result =
                mockMvc.perform(put(QUESTION_URL + "/{questionId}/add-tag/{tagId}", questionIdForSearch, tagIdForSearch)
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status);
    }

    @Test
    public void addTagToQuestion_whenNotSignedIn() throws Exception {
        //given & when
        final ResultActions result =
                mockMvc.perform(put(QUESTION_URL + "/{questionId}/add-tag/{tagId}", savedQuestion.getId(), savedTag.getId())
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("addTagWithStatusesAndRoles")
    public void removeTagFromQuestion_happyPath(final ResultMatcher status, UUID questionId, UUID tagId) throws Exception {
        //given
        final UUID questionIdForSearch = checkIdForSearch(questionId, savedQuestion.getId());
        final UUID tagIdForSearch = checkIdForSearch(tagId, savedTag.getId());

        //when
        final ResultActions result
                = mockMvc.perform(put(QUESTION_URL + "/{questionId}/remove-tag/{tagId}", questionIdForSearch, tagIdForSearch)
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status);
    }

    @Test
    public void removeTagFromQuestion_whenNotSignedIn() throws Exception {
        //given & when
        final ResultActions result =
                mockMvc.perform(put(QUESTION_URL + "/{questionId}/remove-tag/{tagId}", savedQuestion.getId(), savedTag.getId())
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("updateWithEmptyFields")
    public void update_happyPath(final ResultMatcher status, UUID id, final QuestionUpdateRequest questionUpdateRequest,
                                 final boolean isCreator) throws Exception {
        //given
        final SignUpRequest userRequest = generateSignUpRequest();
        insertUser(userRequest);
        TokenResponse activeToken = createSignIn(userRequest);

        final UUID idForSearch = checkIdForSearch(id, savedQuestion.getId());

        activeToken = isCreator(isCreator, activeToken, token);

        //when
        final ResultActions result = mockMvc.perform(put(QUESTION_URL + "/{id}", idForSearch)
                        .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionUpdateRequest)));

        //then
        result.andExpect(status);
    }

    @Test
    public void update_whenNotSignedIn() throws Exception {
        //given & when
        final ResultActions result = mockMvc.perform(put(QUESTION_URL + "/{id}", savedQuestion.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionUpdateRequest)));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void delete_whenNotSignedIn() throws Exception {
        //given & when
        final ResultActions result = mockMvc.perform(delete(QUESTION_URL + "/{id}", savedQuestion.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("deleteWithStatusesRolesAndIds")
    public void deleteById_happyPath(final ResultMatcher status, final UserEntityRole role,
                                     UUID id, final boolean isCreator) throws Exception {
        //given
        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        TokenResponse activeToken = createSignIn(activeUserRequest);

        final UUID idForSearch = checkIdForSearch(id, savedQuestion.getId());

        activeToken = isCreator(isCreator, activeToken, token);

        //when
        final ResultActions result = mockMvc.perform(delete(QUESTION_URL + "/{id}", idForSearch)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status);
    }

    @ParameterizedTest
    @MethodSource("increaseRating")
    public void increaseRatingById_happyPath(final ResultMatcher status, UUID id,
                                             final UserEntityRole role) throws Exception {
        //given
        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        final TokenResponse activeToken = createSignIn(activeUserRequest);

        final UUID idForSearch = checkIdForSearch(id, savedQuestion.getId());

        //when
        final ResultActions result = mockMvc.perform(put(QUESTION_URL + "/{id}/increase-rating", idForSearch)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status);
    }

    @Test
    public void increaseRating_whenNotSignedIn() throws Exception {
        //given & when
        final ResultActions result = mockMvc.perform(put(QUESTION_URL + "/{id}/increase-rating", savedQuestion.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("decreaseRating")
    public void decreaseRatingById_happyPath(final ResultMatcher status, UUID id, final UserEntityRole role) throws Exception {
        //given
        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        final TokenResponse activeToken = createSignIn(activeUserRequest);

        final UUID idForSearch = checkIdForSearch(id, savedQuestion.getId());

        //when
        final ResultActions result = mockMvc.perform(put(QUESTION_URL + "/{id}/decrease-rating", idForSearch)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status);
    }

    static Stream<Arguments> deletedFlagAndSortParams() {
        return Stream.of(
                arguments("false", CREATED_UP, ROLE_USER), arguments("false", CREATED_UP, ROLE_ADMIN),
                arguments("false", CREATED_DOWN, ROLE_USER), arguments("false", CREATED_DOWN, ROLE_ADMIN),
                arguments("false", UPDATED_UP, ROLE_USER), arguments("false", UPDATED_UP, ROLE_ADMIN),
                arguments("false", UPDATE_DOWN, ROLE_USER), arguments("false", UPDATE_DOWN, ROLE_ADMIN),
                arguments("false", RATING_UP, ROLE_USER), arguments("false", RATING_UP, ROLE_ADMIN),
                arguments("false", RATING_DOWN, ROLE_USER), arguments("false", RATING_DOWN, ROLE_ADMIN),
                arguments("false", DELETED_DOWN, ROLE_USER), arguments("false", DELETED_DOWN, ROLE_ADMIN),
                arguments("false", DELETED_UP, ROLE_USER), arguments("false", DELETED_UP, ROLE_ADMIN),
                arguments("true", CREATED_DOWN, ROLE_USER), arguments("true", CREATED_DOWN, ROLE_ADMIN),
                arguments("true", CREATED_UP, ROLE_USER), arguments("true", CREATED_UP, ROLE_ADMIN),
                arguments("true", UPDATE_DOWN, ROLE_USER), arguments("true", UPDATE_DOWN, ROLE_ADMIN),
                arguments("true", UPDATED_UP, ROLE_USER), arguments("true", UPDATED_UP, ROLE_ADMIN),
                arguments("true", RATING_UP, ROLE_USER), arguments("true", RATING_UP, ROLE_ADMIN),
                arguments("true", RATING_DOWN, ROLE_USER), arguments("true", RATING_DOWN, ROLE_ADMIN),
                arguments("true", DELETED_DOWN, ROLE_USER), arguments("true", DELETED_DOWN, ROLE_ADMIN),
                arguments("true", DELETED_UP, ROLE_USER), arguments("true", DELETED_UP, ROLE_ADMIN));
    }
    static Stream<Arguments> createWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isCreated(), ROLE_USER, generateQuestionRequest()),
                arguments(status().isForbidden(), ROLE_ADMIN, generateQuestionRequest()),
                arguments(status().isBadRequest(), ROLE_USER, generateQuestionWithEmptyFields()));
    }

    static Stream<Arguments> addTagWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isOk(), null, null),
                arguments(status().isNotFound(), UUID.randomUUID(), null),
                arguments(status().isNotFound(), null, UUID.randomUUID()));
    }

    static Stream<Arguments> updateWithEmptyFields() {
        return Stream.of(
                arguments(status().isOk(), null, generateQuestionUpdateRequest(), true),
                arguments(status().isNotFound(), UUID.randomUUID(), generateQuestionUpdateRequest(), false),
                arguments(status().isBadRequest(), null, generateQuestionUpdateWithEmptyFields(), true),
                arguments(status().isForbidden(), null, generateQuestionUpdateRequest(), false));
    }

    static Stream<Arguments> deleteWithStatusesRolesAndIds() {
        return Stream.of(
                arguments(status().isNoContent(), ROLE_USER, null, true),
                arguments(status().isForbidden(), ROLE_USER, null, false),
                arguments(status().isNoContent(), ROLE_ADMIN, null, false),
                arguments(status().isNotFound(), ROLE_USER, UUID.randomUUID(), false));
    }
}
