package com.example.answersboxapi.utils;

import com.example.answersboxapi.entity.UserEntity;
import com.example.answersboxapi.enums.UserEntityRole;
import com.example.answersboxapi.model.answer.AnswerRequest;
import com.example.answersboxapi.model.auth.SignInRequest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenRequest;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.tag.TagRequest;
import com.github.javafaker.Faker;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class GeneratorUtil {

    private static final String INVALID_EMAIL = "qwe.rty@gmailcom";
    private static final String INVALID_PASSWORD = "qrty";

    private static final Faker FAKER = new Faker();

    public static UserEntity generateUser() {
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .firstName(FAKER.name().firstName())
                .lastName(FAKER.name().lastName())
                .email(FAKER.internet().emailAddress())
                .password(FAKER.internet().password(true) + 1)
                .createdAt(Instant.now().truncatedTo(ChronoUnit.SECONDS))
                .role(UserEntityRole.ROLE_USER)
                .build();
    }

    public static SignUpRequest generateSignUpRequest() {
        return SignUpRequest.builder()
                .email(FAKER.internet().emailAddress())
                .firstName(FAKER.name().firstName())
                .lastName(FAKER.name().lastName())
                .password(FAKER.internet().password(true) + 1)
                .build();
    }

    public static SignUpRequest generateSignUpRequest(final String email, final String password ) {
        return SignUpRequest.builder()
                .email(email)
                .firstName(FAKER.name().firstName())
                .lastName(FAKER.name().lastName())
                .password(password)
                .build();
    }

    public static SignInRequest generateInvalidSignInRequest() {
        return SignInRequest.builder()
                .email(INVALID_EMAIL)
                .password(INVALID_PASSWORD)
                .build();
    }

    public static SignInRequest generateSignInRequest(final String login, final String password) {
        return SignInRequest.builder()
                .email(login)
                .password(password)
                .build();
    }

    public static SignInRequest generateInvalidSignInRequest(final String email, final String password) {
        return SignInRequest.builder()
                .email(email)
                .password(password)
                .build();
    }

    public static TokenRequest generateTokenRequest(final String accessToken) {
        return TokenRequest.builder()
                .token(accessToken)
                .build();
    }

    public static TagRequest generateTagRequest() {
        return TagRequest.builder()
                .name(FAKER.name().title())
                .build();
    }

    public static QuestionRequest generateQuestionRequest() {
        return QuestionRequest.builder()
                .title(FAKER.name().title())
                .description(FAKER.name().name())
                .build();
    }

    public static QuestionRequest generateQuestionWithEmptyFields() {
        return QuestionRequest.builder()
                .title("")
                .description("")
                .build();
    }

    public static AnswerRequest generateAnswerRequest() {
        return AnswerRequest.builder()
                .text(FAKER.name().title())
                .build();
    }

    public static AnswerRequest generateEmptyAnswer() {
        return AnswerRequest.builder()
                .text("")
                .build();
    }
}
