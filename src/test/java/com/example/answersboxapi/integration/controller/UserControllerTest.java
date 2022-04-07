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
    public void deleteUser(final ResultMatcher status, final UserEntityRole role, UUID id, final boolean isCreator) throws Exception {
        //given
        final SignUpRequest usersRequest = generateSignUpRequest();
        insertUserOrAdmin(usersRequest, role);
        TokenResponse activeToken = createSignIn(usersRequest);

        final UUID idForSearch = checkIdForSearch(id, savedUser.getId());

        activeToken = isCreator(isCreator, activeToken, token);

        //when
        final ResultActions result = mockMvc.perform(delete(USER_URL + "/{id}", idForSearch)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeToken.getAccessToken()));

        //then
        result.andExpect(status);
    }

    static Stream<Arguments> deleteWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isNoContent(), ROLE_USER,  null, true),
                arguments(status().isNoContent(), ROLE_ADMIN,  null, false),
                arguments(status().isNotFound(), ROLE_USER, UUID.randomUUID(), false),
                arguments(status().isForbidden(), ROLE_USER,  null, false));
    }
}
