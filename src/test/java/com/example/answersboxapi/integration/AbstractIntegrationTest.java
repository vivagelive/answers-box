package com.example.answersboxapi.integration;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.entity.QuestionDetailsEntity;
import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.entity.UserEntity;
import com.example.answersboxapi.enums.UserEntityRole;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.answer.AnswerRequest;
import com.example.answersboxapi.model.answer.AnswerUpdateRequest;
import com.example.answersboxapi.model.auth.SignInRequest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionDetails;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.question.QuestionUpdateRequest;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.tag.TagRequest;
import com.example.answersboxapi.model.user.User;
import com.example.answersboxapi.repository.*;
import com.example.answersboxapi.utils.PostgresInitializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static com.example.answersboxapi.enums.UserEntityRole.ROLE_ADMIN;
import static com.example.answersboxapi.enums.UserEntityRole.ROLE_USER;
import static com.example.answersboxapi.mapper.AnswerMapper.ANSWER_MAPPER;
import static com.example.answersboxapi.mapper.QuestionDetailsMapper.QUESTION_DETAILS_MAPPER;
import static com.example.answersboxapi.mapper.QuestionMapper.QUESTION_MAPPER;
import static com.example.answersboxapi.mapper.TagMapper.TAG_MAPPER;
import static com.example.answersboxapi.mapper.UserMapper.USER_MAPPER;
import static com.example.answersboxapi.utils.GeneratorUtil.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = PostgresInitializer.class)
public class AbstractIntegrationTest {

    protected static final String AUTHORIZATION = "Authorization";
    protected static final String TOKEN_PREFIX = "Bearer ";

    protected static final String ANSWER_URL = "/api/v1/answers";
    protected static final String AUTH_URL = "/api/v1/auth";
    protected static final String TAG_URL = "/api/v1/tags";
    protected static final String USER_URL = "/api/v1/users";
    protected static final String QUESTION_URL = "/api/v1/questions";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected AnswerRepository answerRepository;

    @Autowired
    protected QuestionRepository questionRepository;

    @Autowired
    protected QuestionDetailsRepository questionDetailsRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TagRepository tagRepository;

    protected TokenResponse token;
    protected Tag savedTag;
    protected Question savedQuestion;
    protected User savedUser;
    protected Answer savedAnswer;
    protected SignUpRequest signUpRequest;
    protected QuestionRequest questionRequest;
    protected AnswerRequest answerRequest;
    protected TagRequest tagRequest;
    protected AnswerUpdateRequest updateAnswerRequest;
    protected QuestionUpdateRequest questionUpdateRequest;

    @BeforeEach
    protected void fillDataBase() throws Exception {
        answerRequest = generateAnswerRequest();
        signUpRequest = generateSignUpRequest();
        tagRequest = generateTagRequest();
        questionRequest = generateQuestionRequest();
        questionUpdateRequest = generateQuestionUpdateRequest();
        updateAnswerRequest = generateAnswerUpdateRequest();

        savedUser = insertUser(signUpRequest);
        token = createSignIn(signUpRequest);
        savedQuestion = createQuestion(token, questionRequest);
        savedAnswer = createAnswer(savedQuestion.getId(), answerRequest, token);
        savedTag = saveTag();
    }

    @AfterEach
    protected void clearDataBase() {
        userRepository.deleteAll();
    }

    protected User insertUser() {
        return USER_MAPPER.toModel(userRepository.saveAndFlush(generateUser()));
    }

    protected TokenResponse createSignIn(final SignUpRequest signedUpUser) throws Exception {
        final SignInRequest signInRequest = generateSignInRequest(signedUpUser.getEmail(), signedUpUser.getPassword());

        final MvcResult result = mockMvc.perform(post(AUTH_URL + "/sign-in")
                .content(objectMapper.writeValueAsString(signInRequest))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsByteArray(),TokenResponse.class);
    }

    protected User insertUser(final SignUpRequest signUpRequest) {
        final UserEntity userToSave = createEntity(signUpRequest, ROLE_USER);

        return USER_MAPPER.toModel(userRepository.saveAndFlush(userToSave));
    }

    protected User insertUserOrAdmin(final SignUpRequest signUpRequest, final UserEntityRole role) {
        final UserEntity userToSave = createEntity(signUpRequest, role);

        return  USER_MAPPER.toModel(userRepository.saveAndFlush(userToSave));
    }

    protected User insertAdmin(final SignUpRequest signUpRequest) {
        final UserEntity userToSave = createEntity(signUpRequest, ROLE_ADMIN);

        return USER_MAPPER.toModel(userRepository.saveAndFlush(userToSave));
    }

    private UserEntity createEntity(final SignUpRequest signUpRequest, final UserEntityRole role) {
        final String encodedPassword = passwordEncoder.encode(signUpRequest.getPassword());

        final UserEntity userToSave = generateUser();
        userToSave.setEmail(signUpRequest.getEmail());
        userToSave.setPassword(encodedPassword);
        userToSave.setRole(role);

        return userToSave;
    }

    protected Tag createTag(final TokenResponse token, final TagRequest tagRequest) throws Exception {
         final MvcResult result = mockMvc.perform(post(TAG_URL)
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(tagRequest)))
                        .andExpect(status().isCreated())
                        .andReturn();

         return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Tag.class);
    }

    protected Question createQuestion(final TokenResponse token, final QuestionRequest questionRequest) throws Exception {
        final MvcResult result = mockMvc.perform(post(QUESTION_URL)
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionRequest)))
                        .andExpect(status().isCreated())
                        .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Question.class);
    }

    protected Question insertDeletedQuestion(final QuestionRequest questionRequest, final User savedUser) {
        final QuestionEntity questionToSave = QuestionEntity.builder()
                .rating(0)
                .createdAt(Instant.now())
                .deletedAt(Instant.now())
                .title(questionRequest.getTitle())
                .description(questionRequest.getDescription())
                .user(USER_MAPPER.toEntity(savedUser))
                .build();

        return QUESTION_MAPPER.toModel(questionRepository.saveAndFlush(questionToSave));
    }

    protected Answer createAnswer(final UUID questionId, final AnswerRequest answerRequest, final TokenResponse token) throws Exception {
        answerRequest.setQuestionId(questionId);
        final MvcResult result = mockMvc.perform(post(ANSWER_URL)
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(answerRequest)))
                        .andExpect(status().isCreated())
                        .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsByteArray(), Answer.class);
    }

    protected Answer insertDeletedAnswer(final AnswerRequest answerRequest, final User savedUser, final Question savedQuestion) {
        final AnswerEntity answerToSave = AnswerEntity.builder()
                .text(answerRequest.getText())
                .rating(0)
                .user(USER_MAPPER.toEntity(savedUser))
                .question(QUESTION_MAPPER.toEntity(savedQuestion))
                .createdAt(Instant.now())
                .deletedAt(Instant.now())
                .build();

        return ANSWER_MAPPER.toModel(answerRepository.saveAndFlush(answerToSave));
    }

    protected Tag saveTag() throws Exception {
        final SignUpRequest signUpAdminRequest = generateSignUpRequest();
        insertAdmin(signUpAdminRequest);
        final TokenResponse adminsToken = createSignIn(signUpAdminRequest);

        return createTag(adminsToken, generateTagRequest());
    }

    protected QuestionDetails insertQuestionDetails(final Question question, final Tag tag) {
        final QuestionDetailsEntity questionDetails = QuestionDetailsEntity.builder()
                .questionId(QUESTION_MAPPER.toEntity(question))
                .tagId(TAG_MAPPER.toEntity(tag))
                .build();

        return QUESTION_DETAILS_MAPPER.toModel(questionDetailsRepository.saveAndFlush(questionDetails));
    }

    static Stream<Arguments> increaseRating() {
        return Stream.of(
                arguments(status().isOk(), null, ROLE_USER, +1),
                arguments(status().isNotFound(), UUID.randomUUID(), ROLE_USER, 0),
                arguments(status().isForbidden(), null, UserEntityRole.ROLE_ADMIN, 0));
    }

    static Stream<Arguments> decreaseRating() {
        return Stream.of(
                arguments(status().isOk(), null, ROLE_USER, -1),
                arguments(status().isNotFound(), UUID.randomUUID(), ROLE_USER, 0),
                arguments(status().isForbidden(), null, UserEntityRole.ROLE_ADMIN, 0));
    }
    protected UUID checkIdForSearch (UUID idForSearch, final UUID savedId) {
        if (idForSearch == null) {
            idForSearch = savedId;
        }
        return idForSearch;
    }

    protected TokenResponse isCreator (final boolean isCreator, TokenResponse activeToken, final TokenResponse token) {
        if (isCreator) {
            activeToken = token;
        }
        return activeToken;
    }
}
