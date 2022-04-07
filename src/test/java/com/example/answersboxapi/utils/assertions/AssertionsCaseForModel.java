package com.example.answersboxapi.utils.assertions;

import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.user.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
        assertAll(
                () -> assertNotNull(createdAnswer),
                () -> assertEquals(createdAnswer.getUserId(), savedUser.getId()),
                () -> assertEquals(createdAnswer.getQuestionId(), savedQuestion.getId())
        );
    }

    public static void assertQuestionsListFields(final List<Question> foundQuestions, final User user, final Question savedQuestion) {
        assertAll(
                () -> assertEquals(user.getId(), foundQuestions.stream().findFirst().get().getUserId()),
                () -> assertEquals(savedQuestion.getId(), foundQuestions.stream().findFirst().get().getId()),
                () -> assertEquals(savedQuestion.getTitle(), foundQuestions.stream().findFirst().get().getTitle()),
                () -> assertEquals(savedQuestion.getDescription(), foundQuestions.stream().findFirst().get().getDescription())
        );
    }

    public static void assertAnswersListFields(final List<Answer> foundAnswers, final Answer savedAnswer) {
        assertAll(
                () -> assertEquals(savedAnswer.getId(), foundAnswers.stream().findFirst().get().getId()),
                () -> assertEquals(savedAnswer.getText(), foundAnswers.stream().findFirst().get().getText()),
                () -> assertEquals(savedAnswer.getRating(), foundAnswers.stream().findFirst().get().getRating()),
                () -> assertEquals(savedAnswer.getUserId(), foundAnswers.stream().findFirst().get().getUserId()),
                () -> assertEquals(savedAnswer.getQuestionId(), foundAnswers.stream().findFirst().get().getQuestionId())
        );
    }

    public static void assertQuestionFields(final Question foundQuestion, final User savedUser, final Tag savedTag){
        assertAll(
                () -> assertNotNull(foundQuestion.getTagsIds()),
                () -> assertEquals(savedUser.getId(), foundQuestion.getUserId()));
    }

    public static void assertQuestionUpdatedFields(final Question updatedQuestion, final Question savedQuestion) {
        assertAll(
                () -> assertNotNull(updatedQuestion),
                () -> assertEquals(savedQuestion.getId(), updatedQuestion.getId()),
                () -> assertEquals(savedQuestion.getUserId(), updatedQuestion.getUserId()),
                () -> assertNotEquals(savedQuestion.getTitle(), updatedQuestion.getTitle()),
                () -> assertNotEquals(savedQuestion.getDescription(), updatedQuestion.getDescription())
        );
    }
}
