package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.tag.TagRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static com.example.answersboxapi.utils.GeneratorUtil.generateSignUpRequest;
import static com.example.answersboxapi.utils.GeneratorUtil.generateTagRequest;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TagControllerTest extends AbstractIntegrationTest {

    @Test
    public void create_happyPath() throws Exception {
        //given
        final TagRequest tagRequest = generateTagRequest();

        final SignUpRequest signUpRequest = generateSignUpRequest();
        createAdmin(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        //when
        final MvcResult result = mockMvc.perform(post(TAG_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(tagRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        final Tag tag = createTagFromResponse(result);

        //then
        assertEquals(tagRequest.getName(), tag.getName());
    }

    @Test
    public void create_withUserAccess() throws Exception {
        //given
        final TagRequest tagRequest = generateTagRequest();

        final SignUpRequest signUpRequest = generateSignUpRequest();
        createUser(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        //when
        final ResultActions result = mockMvc.perform(post(TAG_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(tagRequest)));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    public void create_whenNotSignedIn() throws Exception {
        //given
        final TagRequest tagRequest = generateTagRequest();

        final SignUpRequest signUpRequest = generateSignUpRequest();
        createUser(signUpRequest);

        //when
        final ResultActions result = mockMvc.perform(post(TAG_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(tagRequest)));

        //then
        result.andExpect(status().isForbidden());
    }
}
