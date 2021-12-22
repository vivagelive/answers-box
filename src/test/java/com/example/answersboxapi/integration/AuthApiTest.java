package com.example.answersboxapi.integration;

import com.example.answersboxapi.model.User;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.utils.assertions.AssertionsCaseForModel;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.answersboxapi.utils.GeneratorUtil.generateSignUpRequest;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthApiTest extends AbstractIntegrationTest {

    @Test
    public void signUp_happyPath() throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();

        //when
        final MvcResult result = mockMvc.perform(post(AUTH_URL + "/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                        .andExpect(status().isCreated())
                        .andReturn();

        final User response = objectMapper.readValue(result.getResponse().getContentAsByteArray(), User.class);

        //then
        assertAll(
                () -> AssertionsCaseForModel.assertUsersFieldsNotNull(response),
                () -> AssertionsCaseForModel.assertUsersFieldsEquals(signUpRequest, response)
        );
    }

    @Test
    public void signUp_whenEmailExist() throws Exception {
        //given
        final User savedUser = createUser();

        final SignUpRequest signUpRequest = generateSignUpRequest(savedUser.getEmail());

        //when
        final ResultActions resultAction = mockMvc.perform(post(AUTH_URL + "/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)));

        //then
        resultAction.andExpect(status().isUnprocessableEntity());
    }
}
