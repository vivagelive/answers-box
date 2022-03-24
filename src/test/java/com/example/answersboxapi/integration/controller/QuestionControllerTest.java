package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.SortParams;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.answer.AnswerRequest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.question.QuestionUpdateRequest;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.user.User;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.example.answersboxapi.model.SortParams.*;
import static com.example.answersboxapi.utils.GeneratorUtil.*;
import static com.example.answersboxapi.utils.assertions.AssertionsCaseForModel.*;
import static com.example.answersboxapi.utils.pagination.PagingUtils.toPageRequest;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @ParameterizedTest
    @MethodSource("deletedFlagAndSortParams")
    public void getAll_SortedWithUserAccessAndDeletedFlag(final String deletedFlag, final SortParams sortParams) throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        final QuestionRequest questionRequest = generateQuestionRequest();

        final Tag savedTag = saveTag();

        final Question savedQuestion = createQuestion(token, questionRequest);
        final Question deletedQuestion = insertDeletedQuestion(generateQuestionRequest(), savedUser);

        insertQuestionDetails(savedQuestion, savedTag);
        insertQuestionDetails(deletedQuestion, savedTag);

        //when
        final ResultActions result = mockMvc.perform(get(QUESTION_URL + "/all")
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
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
    public void getAll_SortedWithAdminAccessAndDeletedFlag(final String deletedFlag, final SortParams sortParams) throws Exception {
        //given
        final SignUpRequest signUpUserRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpUserRequest);
        final TokenResponse usersToken = createSignIn(signUpUserRequest);

        final SignUpRequest signUpAdminRequest = generateSignUpRequest();
        insertAdmin(signUpAdminRequest);
        final TokenResponse adminsToken = createSignIn(signUpAdminRequest);

        final QuestionRequest questionRequest = generateQuestionRequest();

        final Tag savedTag = saveTag();

        final Question savedQuestion = createQuestion(usersToken, questionRequest);

        final QuestionRequest deletedQuestionRequest = generateQuestionRequest();
        deletedQuestionRequest.setTitle(savedQuestion.getTitle());
        final Question deletedQuestion = insertDeletedQuestion(deletedQuestionRequest, savedUser);

        insertQuestionDetails(savedQuestion, savedTag);
        insertQuestionDetails(deletedQuestion, savedTag);

        //when
        final ResultActions result = mockMvc.perform(get(QUESTION_URL + "/all")
                        .header(AUTHORIZATION, TOKEN_PREFIX + adminsToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("tagIds", savedTag.getId().toString())
                        .param("deletedFlag", deletedFlag)
                        .param("sortParams", sortParams.toString()))
                        .andExpect(status().isOk());

        final List<Question> foundQuestions =
                objectMapper.readValue(result.andReturn().getResponse().getContentAsByteArray(), new TypeReference<>() {});

        //then
        assertAll(
                () -> assertEquals(1, foundQuestions.size())
        );
    }

    @ParameterizedTest
    @MethodSource("deletedFlagAndSortParams")
    public void getAnswersByQuestionId_SortedWithUserAccessAndDeletedFlag
            (final String deletedFlag, final SortParams sortParams) throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final QuestionRequest questionRequest = generateQuestionRequest();

        final Question savedQuestion = createQuestion(token, questionRequest);

        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), token);
        insertDeletedAnswer(generateAnswerRequest(), savedUser, savedQuestion);

        //when
        final MvcResult result = mockMvc.perform(get(format(QUESTION_URL + "/%s/answers", savedQuestion.getId()))
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isDeleted", deletedFlag)
                        .param("sortParams", sortParams.toString()))
                        .andExpect(status().isOk())
                        .andReturn();

        final List<Answer> foundAnswers =
                objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>() {});

        //then
        assertAll(
                () -> assertEquals(1, foundAnswers.size()),
                () -> assertAnswersListFields(foundAnswers, savedAnswer),
                () -> assertNull(foundAnswers.stream().findFirst().get().getDeletedAt())
        );
    }

    @ParameterizedTest
    @MethodSource({"deletedFlagAndSortParams"})
    public void getAnswersByQuestionId_SortedWithAdminAccessAndDeletedFlag
            (final String deletedFlag, final SortParams sortParams) throws Exception {
        //given
        final SignUpRequest signUpUserRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpUserRequest);
        final TokenResponse usersToken = createSignIn(signUpUserRequest);

        final SignUpRequest signUpAdminRequest = generateSignUpRequest();
        insertAdmin(signUpAdminRequest);
        final TokenResponse adminsToken = createSignIn(signUpAdminRequest);

        final QuestionRequest questionRequest = generateQuestionRequest();

        final Question savedQuestion = createQuestion(usersToken, questionRequest);

        final Answer savedAnswer = createAnswer(savedQuestion.getId(), generateAnswerRequest(), usersToken);

        final AnswerRequest deletedAnswerRequest = generateAnswerRequest();
        deletedAnswerRequest.setText(savedAnswer.getText());
        insertDeletedAnswer(deletedAnswerRequest, savedUser, savedQuestion);

        //when
        final MvcResult result = mockMvc.perform(get(format(QUESTION_URL + "/%s/answers", savedQuestion.getId()))
                        .header(AUTHORIZATION, TOKEN_PREFIX + adminsToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("isDeleted", deletedFlag)
                        .param("sortParams", sortParams.toString()))
                        .andExpect(status().isOk())
                        .andReturn();

        final List<Answer> foundAnswers =
                objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<>() {});

        //then
        assertAll(
                () -> assertEquals(1, foundAnswers.size())
        );
    }

    @Test
    public void addTagToQuestion_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User savedUser = insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        final Tag savedTag = saveTag();

        //when
        final MvcResult result = mockMvc.perform(put(QUESTION_URL + "/{questionId}/add-tag/{tagId}", savedQuestion.getId(), savedTag.getId())
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        final Question foundQuestion = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Question.class);

        //then
        assertAll(
                () -> assertEquals(1, foundQuestion.getTagsIds().size()),
                () -> assertQuestionFields(foundQuestion, savedUser, savedTag)
        );
    }

    @Test
    public void addTagToQuestion_whenTagNotFound() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        final UUID notExistingId = UUID.randomUUID();

        //when
        final ResultActions result =
                mockMvc.perform(put(QUESTION_URL + "/{questionId}/add-tag/{tagId}", savedQuestion.getId(), notExistingId)
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    public void addTagToQuestion_whenQuestionNotFound() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Tag savedTag = saveTag();

        final UUID notExistingId = UUID.randomUUID();

        //when
        final ResultActions result =
                mockMvc.perform(put(QUESTION_URL + "/{questionId}/add-tag/{tagId}", notExistingId, savedTag.getId())
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    public void addTagToQuestion_whenNotSignedIn() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        final Tag savedTag = saveTag();

        //when
        final ResultActions result =
                mockMvc.perform(put(QUESTION_URL + "/{questionId}/add-tag/{tagId}", savedQuestion.getId(), savedTag.getId())
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void removeTagFromQuestion_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        final Tag savedTag = saveTag();

        insertQuestionDetails(savedQuestion, savedTag);

        //when
        final MvcResult result
                = mockMvc.perform(put(QUESTION_URL + "/{questionId}/remove-tag/{tagId}", savedQuestion.getId(), savedTag.getId())
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn();

        final Question foundQuestion = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Question.class);

        //then
        assertAll(
                () -> assertNotNull(foundQuestion),
                () -> assertTrue(foundQuestion.getTagsIds().isEmpty()),
                () -> assertTrue(questionDetailsRepository.findAllByQuestionId(savedQuestion.getId()).isEmpty())
        );
    }

    @Test
    public void removeTagFromQuestion_whenQuestionNotFound() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Tag savedTag = saveTag();

        final UUID notExistingId = UUID.randomUUID();

        //when
        final ResultActions result
                = mockMvc.perform(put(QUESTION_URL + "/{questionId}/remove-tag/{tagId}", notExistingId, savedTag.getId())
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    public void removeTagFromQuestion_whenTagNotFound() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        final UUID notExistingId = UUID.randomUUID();

        //when
        final ResultActions result
                = mockMvc.perform(put(QUESTION_URL + "/{questionId}/remove-tag/{tagId}", savedQuestion.getId(), notExistingId)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    public void removeTagFromQuestion_whenNotSignedIn() throws Exception {
        //given
        SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        final Tag savedTag = saveTag();

        //when
        final ResultActions result =
                mockMvc.perform(put(QUESTION_URL + "/{questionId}/remove-tag/{tagId}", savedQuestion.getId(), savedTag.getId())
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void update_happyPath() throws Exception {
        //given
        SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        final QuestionUpdateRequest questionUpdateRequest = generateQuestionUpdateRequest();

        //when
        final MvcResult result = mockMvc.perform(put(QUESTION_URL + "/{id}", savedQuestion.getId())
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionUpdateRequest)))
                        .andExpect(status().isOk())
                        .andReturn();

        final Question updatedQuestion = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Question.class);

        //then
        assertQuestionUpdatedFields(updatedQuestion, savedQuestion);
    }

    @Test
    public void update_whenUserNotCreatorOfQuestion() throws Exception {
        //given
        SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse questionCreatorToken = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(questionCreatorToken, generateQuestionRequest());

        SignUpRequest userRequest = generateSignUpRequest();
        insertUser(userRequest);
        final TokenResponse userToken = createSignIn(userRequest);

        final QuestionUpdateRequest questionUpdateRequest = generateQuestionUpdateRequest();

        //when
        final ResultActions result = mockMvc.perform(put(QUESTION_URL + "/{id}", savedQuestion.getId())
                        .header(AUTHORIZATION, TOKEN_PREFIX + userToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionUpdateRequest)));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    public void update_whenQuestionNotFound() throws Exception {
        //given
        SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final QuestionUpdateRequest questionUpdateRequest = generateQuestionUpdateRequest();

        final UUID notExistingId = UUID.randomUUID();

        //when
        final ResultActions result = mockMvc.perform(put(QUESTION_URL + "/{id}", notExistingId)
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionUpdateRequest)));

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    public void update_whenNotSignedIn() throws Exception {
        //given
        SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        final QuestionUpdateRequest questionUpdateRequest = generateQuestionUpdateRequest();

        //when
        final ResultActions result = mockMvc.perform(put(QUESTION_URL + "/{id}", savedQuestion.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionUpdateRequest)));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void update_withEmptyFields() throws Exception {
        //given
        SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        final QuestionUpdateRequest questionUpdateRequest = generateQuestionUpdateWithEmptyFields();

        //when
        final ResultActions result = mockMvc.perform(put(QUESTION_URL + "/{id}", savedQuestion.getId())
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionUpdateRequest)));

        //then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void deleteById_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        final AnswerRequest answerRequest = generateAnswerRequest();
        final Answer savedAnswer = createAnswer(savedQuestion.getId(), answerRequest ,token);
        savedQuestion.setAnswerIds(List.of(savedAnswer.getId()));

        //when
        mockMvc.perform(delete(QUESTION_URL + "/{id}", savedQuestion.getId())
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNoContent())
                        .andReturn();

        final Page<AnswerEntity> foundAnswers =
                answerRepository.findAllByQuestionId(savedQuestion.getId(), savedAnswer.getText(), false,
                        toPageRequest(1, 10));

        //then
        assertEquals(0, foundAnswers.getContent().size());
    }

    @Test
    public void delete_whenNotSignedIn() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        //when
        final ResultActions result = mockMvc.perform(delete(QUESTION_URL + "/{id}", savedQuestion.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteById_whenUserNotCreatorOfQuestion() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse questionCreatorToken = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(questionCreatorToken, generateQuestionRequest());

        final SignUpRequest userRequest = generateSignUpRequest();
        insertUser(userRequest);
        final TokenResponse userToken = createSignIn(userRequest);

        //when
        final ResultActions result = mockMvc.perform(delete(QUESTION_URL + "/{id}", savedQuestion.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + userToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    public void deleteById_withAdminAccess() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse questionCreatorToken = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(questionCreatorToken, generateQuestionRequest());

        final SignUpRequest adminRequest = generateSignUpRequest();
        insertAdmin(adminRequest);
        final TokenResponse adminsToken = createSignIn(adminRequest);

        //when
        final ResultActions result = mockMvc.perform(delete(QUESTION_URL + "/{id}", savedQuestion.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + adminsToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNoContent());
    }

    @Test
    public void delete_whenQuestionNotFound() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final UUID notExistedId = UUID.randomUUID();

        //when
        final ResultActions result = mockMvc.perform(delete(QUESTION_URL + "/{id}", notExistedId)
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    public void increaseRatingById_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        //when
        mockMvc.perform(put(QUESTION_URL + "/{id}/increase-rating", savedQuestion.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        final QuestionEntity foundQuestion = questionRepository.getById(savedQuestion.getId());

        //then
        assertEquals(savedQuestion.getRating() + 1, foundQuestion.getRating());
    }

    @Test
    public void increaseRating_whenQuestionNotFound() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        createQuestion(token, generateQuestionRequest());

        final UUID notExistingId = UUID.randomUUID();

        //when
        final ResultActions result = mockMvc.perform(put(QUESTION_URL + "/{id}/increase-rating", notExistingId)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    public void increaseRating_whenNotSignedIn() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        //when
        final ResultActions result = mockMvc.perform(put(QUESTION_URL + "/{id}/increase-rating", savedQuestion.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void increaseRating_withAdminAccess() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUser(signUpRequest);
        final TokenResponse token = createSignIn(signUpRequest);

        final Question savedQuestion = createQuestion(token, generateQuestionRequest());

        final SignUpRequest adminsRequest = generateSignUpRequest();
        insertAdmin(adminsRequest);
        final TokenResponse adminsToken = createSignIn(adminsRequest);

        //when
        final ResultActions result = mockMvc.perform(put(QUESTION_URL + "/{id}/increase-rating", savedQuestion.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + adminsToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isForbidden());
    }

    static Stream<Arguments> deletedFlagAndSortParams() {
        return Stream.of(
                arguments("false", CREATED_UP),
                arguments("false", CREATED_DOWN),
                arguments("false", UPDATED_UP),
                arguments("false", UPDATE_DOWN),
                arguments("false", RATING_UP),
                arguments("false", RATING_DOWN),
                arguments("false", DELETED_DOWN),
                arguments("false", DELETED_UP),
                arguments("true", CREATED_DOWN),
                arguments("true", CREATED_UP),
                arguments("true", UPDATE_DOWN),
                arguments("true", UPDATED_UP),
                arguments("true", DELETED_DOWN),
                arguments("true", DELETED_UP));
    }
}
