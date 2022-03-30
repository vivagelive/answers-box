package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.enums.UserEntityRole;
import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.user.User;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.UUID;
import java.util.stream.Stream;

import static com.example.answersboxapi.enums.UserEntityRole.ROLE_ADMIN;
import static com.example.answersboxapi.enums.UserEntityRole.ROLE_USER;
import static com.example.answersboxapi.utils.GeneratorUtil.generateSignUpRequest;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerTest extends AbstractIntegrationTest {

    @ParameterizedTest
    @MethodSource("deleteWithStatusesAndRoles")
    public void deleteById_withUserAndAdminsAccess(final ResultMatcher status, final UserEntityRole role) throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User savedUser = insertUserOrAdmin(signUpRequest, role);

        final TokenResponse token = createSignIn(signUpRequest);

        //when
        final ResultActions result = mockMvc.perform(delete(USER_URL + "/{id}", savedUser.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken()));

        //then
        result.andExpect(status);
    }

    @ParameterizedTest
    @MethodSource("deleteWithStatusesAndId")
    public void deleteById_whenWrongIdOrForbidden(final ResultMatcher status, UUID idForSearch) throws Exception {
        //given
        final User savedUser = insertUser(generateSignUpRequest());

        final SignUpRequest activeUser = generateSignUpRequest();
        insertUser(activeUser);
        final TokenResponse token = createSignIn(activeUser);

        if (idForSearch == null) {
            idForSearch = savedUser.getId();
        }

        //when
        final ResultActions result = mockMvc.perform(delete(USER_URL + "/{id}", idForSearch)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken()));

        //then
        result.andExpect(status);
    }

    static Stream<Arguments> deleteWithStatusesAndId() {
        return Stream.of(
                arguments(status().isNotFound(), UUID.randomUUID()),
                arguments(status().isForbidden(), null));
    }

    static Stream<Arguments> deleteWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isNoContent(), ROLE_USER, generateSignUpRequest()),
                arguments(status().isNoContent(), ROLE_ADMIN, generateSignUpRequest()));
    }
}
