package com.example.answersboxapi.utils.assertions;

import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.user.User;
import com.example.answersboxapi.model.auth.SignUpRequest;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertionsCaseForModel {

    public static void assertUsersFieldsNotNull(final User user) {
        assertAll(
                () -> assertNotNull(user.getId()),
                () -> assertNotNull(user.getRole()),
                () -> assertNotNull(user.getEmail()),
                () -> assertNotNull(user.getPassword()),
                () -> assertNotNull(user.getLastName()),
                () -> assertNotNull(user.getFirstName())
        );
    }

    public static void assertUsersFieldsEquals(final SignUpRequest signUpRequest, final User foundUser) {
        assertAll(
                () -> assertUsersFieldsNotNull(foundUser),
                () -> assertEquals(signUpRequest.getEmail(), foundUser.getEmail()),
                () -> assertEquals(signUpRequest.getLastName(), foundUser.getLastName()),
                () -> assertNotEquals(signUpRequest.getPassword(), foundUser.getPassword()),
                () -> assertEquals(signUpRequest.getFirstName(), foundUser.getFirstName())
        );
    }

    public static void assertAnswerFieldsEquals(final Answer createdAnswer, final User savedUser, final Question savedQuestion) {
        Assertions.assertAll(
                () -> Assertions.assertNotNull(createdAnswer),
                () -> Assertions.assertEquals(createdAnswer.getUserId(), savedUser.getId()),
                () -> Assertions.assertEquals(createdAnswer.getQuestionId(), savedQuestion.getId())
        );
    }
}
