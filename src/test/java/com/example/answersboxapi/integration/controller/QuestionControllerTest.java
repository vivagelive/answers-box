package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.enums.UserEntityRole;
import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.SortParams;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.question.QuestionUpdateRequest;
import com.example.answersboxapi.model.user.User;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.example.answersboxapi.enums.UserEntityRole.ROLE_ADMIN;
import static com.example.answersboxapi.enums.UserEntityRole.ROLE_USER;
import static com.example.answersboxapi.model.SortParams.*;
import static com.example.answersboxapi.utils.GeneratorUtil.*;
import static com.example.answersboxapi.utils.assertions.AssertionsCaseForModel.assertQuestionFields;
import static com.example.answersboxapi.utils.assertions.AssertionsCaseForModel.assertQuestionsListFields;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class QuestionControllerTest extends AbstractIntegrationTest {

    @ParameterizedTest
    @MethodSource("createWithStatusesAndRoles")
    public void createQuestion(final ResultMatcher status, final UserEntityRole role,
                               final QuestionRequest questionRequest, final boolean happyCondition) throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User activeUser = insertUserOrAdmin(signUpRequest, role);

        final TokenResponse token = createSignIn(signUpRequest);

        //when
        final MvcResult result = mockMvc.perform(post(QUESTION_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(questionRequest)))
                .andExpect(status)
                .andReturn();

        //then
        assertCondition(happyCondition, result, activeUser);
    }

    @ParameterizedTest
    @MethodSource("deletedFlagAndSortParams")
    public void getAllQuestions(final String deletedFlag, final SortParams sortParams, final UserEntityRole role) throws Exception {
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
    public void getAnswersByQuestionId(final String deletedFlag, final SortParams sortParams,
                                        final UserEntityRole role) throws Exception {
        //given
        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        TokenResponse activeToken = createSignIn(activeUserRequest);

        insertDeletedAnswer(generateAnswerRequest(), savedUser, savedQuestion);

        //when
        final MvcResult result = mockMvc.perform(get(QUESTION_URL + "/{id}/answers", savedQuestion.getId())
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
    public void addTagToQuestion(final ResultMatcher status, UUID questionId, UUID tagId, final boolean happyCondition) throws Exception {
        //given
        final UUID questionIdForSearch = checkIdForSearch(questionId, savedQuestion.getId());
        final UUID tagIdForSearch = checkIdForSearch(tagId, savedTag.getId());

        //when
        final MvcResult result =
                mockMvc.perform(put(QUESTION_URL + "/{questionId}/add-tag/{tagId}", questionIdForSearch, tagIdForSearch)
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status)
                        .andReturn();

        // then
        assertCondition(happyCondition, result, savedUser);
    }

    @ParameterizedTest
    @MethodSource("httpMethodsWithUrls")
    public void questionEndpoints_whenNotSignedIn(final HttpMethod method, final String url) throws Exception {
        //given & when
        final ResultActions result =
                mockMvc.perform(request(method, QUESTION_URL + url, savedQuestion.getId(), savedTag.getId())
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("addTagWithStatusesAndRoles")
    public void removeTagFromQuestion(final ResultMatcher status, UUID questionId, UUID tagId, final boolean happyCondition) throws Exception {
        //given
        final UUID questionIdForSearch = checkIdForSearch(questionId, savedQuestion.getId());
        final UUID tagIdForSearch = checkIdForSearch(tagId, savedTag.getId());

        //when
        final MvcResult result
                = mockMvc.perform(put(QUESTION_URL + "/{questionId}/remove-tag/{tagId}", questionIdForSearch, tagIdForSearch)
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status)
                        .andReturn();

        //then
        assertCondition(happyCondition, result, savedUser);
    }

    @ParameterizedTest
    @MethodSource("updateWithEmptyFields")
    public void updateQuestion(final ResultMatcher status, UUID id, final QuestionUpdateRequest questionUpdateRequest,
                               final boolean isCreator, final boolean happyCondition) throws Exception {
        //given
        final SignUpRequest userRequest = generateSignUpRequest();
        insertUserOrAdmin(userRequest, ROLE_USER);
        TokenResponse activeToken = createSignIn(userRequest);

        final UUID idForSearch = checkIdForSearch(id, savedQuestion.getId());

        activeToken = isCreator(isCreator, activeToken, token);

        //when
        final MvcResult result = mockMvc.perform(put(QUESTION_URL + "/{id}", idForSearch)
                        .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionUpdateRequest)))
                        .andExpect(status)
                        .andReturn();

        //then
        assertCondition(happyCondition, result, savedUser);
    }

    @ParameterizedTest
    @MethodSource("deleteWithStatusesRolesAndIds")
    public void deleteQuestion(final ResultMatcher status, final UserEntityRole role,
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
    public void increaseQuestionRating(final ResultMatcher status, UUID id, final UserEntityRole role,
                                       final Integer increaseDelta, final boolean happyCondition) throws Exception {
        //given
        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        final TokenResponse activeToken = createSignIn(activeUserRequest);

        final UUID idForSearch = checkIdForSearch(id, savedQuestion.getId());

        //when
        final MvcResult result = mockMvc.perform(put(QUESTION_URL + "/{id}/increase-rating", idForSearch)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andReturn();

        final QuestionEntity foundQuestion = questionRepository.getById(savedQuestion.getId());

        //then
        assertAll(
                () -> assertEquals(savedQuestion.getRating() + increaseDelta, foundQuestion.getRating()),
                () -> assertCondition(happyCondition, result, savedUser));
    }

    @ParameterizedTest
    @MethodSource("decreaseRating")
    public void decreaseQuestionRating_happyPath(final ResultMatcher status, UUID id, final UserEntityRole role,
                                                 final Integer decreaseDelta, final boolean happyCondition) throws Exception {
        //given
        final SignUpRequest activeUserRequest = generateSignUpRequest();
        insertUserOrAdmin(activeUserRequest, role);
        final TokenResponse activeToken = createSignIn(activeUserRequest);

        final UUID idForSearch = checkIdForSearch(id, savedQuestion.getId());

        //when
        final MvcResult result = mockMvc.perform(put(QUESTION_URL + "/{id}/decrease-rating", idForSearch)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andReturn();

        final QuestionEntity foundQuestion = questionRepository.getById(savedQuestion.getId());

        //then
        assertAll(
                () -> assertEquals(savedQuestion.getRating() + decreaseDelta, foundQuestion.getRating()),
                () -> assertCondition(happyCondition, result, savedUser));
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
                arguments(status().isCreated(), ROLE_USER, generateQuestionRequest(), true),
                arguments(status().isForbidden(), ROLE_ADMIN, generateQuestionRequest(), false),
                arguments(status().isBadRequest(), ROLE_USER, generateQuestionWithEmptyFields(), false));
    }

    static Stream<Arguments> addTagWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isOk(), null, null, true),
                arguments(status().isNotFound(), UUID.randomUUID(), null, false),
                arguments(status().isNotFound(), null, UUID.randomUUID(), false));
    }

    static Stream<Arguments> updateWithEmptyFields() {
        return Stream.of(
                arguments(status().isOk(), null, generateQuestionUpdateRequest(), true, true),
                arguments(status().isNotFound(), UUID.randomUUID(), generateQuestionUpdateRequest(), false, false),
                arguments(status().isBadRequest(), null, generateQuestionUpdateWithEmptyFields(), true, false),
                arguments(status().isForbidden(), null, generateQuestionUpdateRequest(), false, false));
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
                arguments(HttpMethod.PUT, "/{questionId}/add-tag/{tagId}"),
                arguments(HttpMethod.PUT, "/{questionId}/remove-tag/{tagId}"),
                arguments(HttpMethod.PUT, "/{id}/increase-rating"),
                arguments(HttpMethod.PUT,"/{id}/decrease-rating"));
    }

    private void assertCondition(final boolean happyCondition, final MvcResult result, final User activeUser) throws IOException {
        if (happyCondition){
            final Question foundQuestion = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Question.class);

            assertQuestionFields(foundQuestion, activeUser, savedTag);
        }
    }
}
