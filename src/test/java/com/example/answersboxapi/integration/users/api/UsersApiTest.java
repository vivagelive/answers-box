package com.example.answersboxapi.integration.users.api;

import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.User;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static com.example.answersboxapi.utils.GeneratorUtil.generateSignUpRequest;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UsersApiTest extends AbstractIntegrationTest {

    @Test
    public void deleteById_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        final User savedUser = createUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        //when
        final ResultActions result = mockMvc.perform(delete(USER_URL + "/{id}", savedUser.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken()));

        //then
        result.andExpect(status().isNoContent());
    }

    @Test
    public void deleteByID_whenWrongId() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();
        createUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        // when
        final ResultActions result = mockMvc.perform(delete(USER_URL + "/{id}", UUID.randomUUID())
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken()));

        //then
        result.andExpect(status().isNotFound());
    }

    @Test
    public void deleteById_whenForbidden() throws Exception {
        //given
        final User savedUser = createUser(generateSignUpRequest());

        final SignUpRequest activeUser = generateSignUpRequest();
        createUser(activeUser);
        final TokenResponse token = createSignIn(activeUser);

        //when
        final ResultActions result = mockMvc.perform(delete(USER_URL + "/{id}", savedUser.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken()));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    public void deleteById_withAdminAccess() throws Exception {
        //given
        final User savedUser = createUser(generateSignUpRequest());

        final SignUpRequest signUpRequest = generateSignUpRequest();
        createAdmin(signUpRequest);

        final TokenResponse adminsToken = createSignIn(signUpRequest);

        //when
        final ResultActions result = mockMvc.perform(delete(USER_URL + "/{id}", savedUser.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + adminsToken.getAccessToken()));

        //then
        result.andExpect(status().isNoContent());
    }
}
