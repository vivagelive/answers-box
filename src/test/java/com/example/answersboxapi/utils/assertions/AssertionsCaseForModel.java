package com.example.answersboxapi.utils.assertions;

import com.example.answersboxapi.model.User;
import com.example.answersboxapi.model.auth.SignUpRequest;

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
}
