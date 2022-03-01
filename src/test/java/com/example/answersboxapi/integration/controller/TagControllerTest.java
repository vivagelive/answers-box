package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.entity.TagEntity;
import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.tag.TagRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static com.example.answersboxapi.utils.GeneratorUtil.generateSignUpRequest;
import static com.example.answersboxapi.utils.GeneratorUtil.generateTagRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TagControllerTest extends AbstractIntegrationTest {

    @Test
    public void create_happyPath() throws Exception {
        //given
        final TagRequest tagRequest = generateTagRequest();

        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertAdmin(signUpRequest);

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
        insertUser(signUpRequest);

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
        insertUser(signUpRequest);

        //when
        final ResultActions result = mockMvc.perform(post(TAG_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(tagRequest)));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void create_whenDuplicate() throws Exception {
        //given
        final TagRequest tagRequest = generateTagRequest();

        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertAdmin(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);

        //when
        createTag(token, tagRequest);

        final ResultActions result = mockMvc.perform(post(TAG_URL)
                        .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(tagRequest)));

        //then
        result.andExpect(status().isUnprocessableEntity());
    }

    @Test
    public void deleteById_happyPath() throws Exception {
        //given
        final TagRequest tagRequest = generateTagRequest();

        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertAdmin(signUpRequest);

        final TokenResponse adminsToken = createSignIn(signUpRequest);
        final Tag savedTag = createTag(adminsToken, tagRequest);

        //when
        final MvcResult result = mockMvc.perform(delete(TAG_URL + "/{id}", savedTag.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + adminsToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        final TagEntity foundTag = tagRepository.getById(savedTag.getId());

        //then
        assertNotNull(foundTag);
    }

    @Test
    public void deleteById_whenNotSignedIn() throws Exception {
        //given
        final TagRequest tagRequest = generateTagRequest();

        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertAdmin(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);
        final Tag savedTag = createTag(token, tagRequest);

        //when
        final ResultActions result = mockMvc.perform(delete(TAG_URL + "/{id}", savedTag.getId())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void deleteById_withUserAccess() throws Exception {
        //given
        final TagRequest tagRequest = generateTagRequest();

        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertAdmin(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);
        final Tag savedTag = createTag(token, tagRequest);

        final SignUpRequest usersRequest = generateSignUpRequest();
        insertUser(usersRequest);

        final TokenResponse usersToken = createSignIn(usersRequest);

        //when
        final ResultActions result = mockMvc.perform(delete(TAG_URL + "/{id}", savedTag.getId())
                .header(AUTHORIZATION, TOKEN_PREFIX + usersToken.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isForbidden());
    }

    @Test
    public void deleteById_whenTagNotFound() throws Exception {
        //given
        final TagRequest tagRequest = generateTagRequest();

        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertAdmin(signUpRequest);

        final TokenResponse token = createSignIn(signUpRequest);
        createTag(token, tagRequest);

        final UUID notExistingId = UUID.randomUUID();

        //when
        final ResultActions result = mockMvc.perform(delete(TAG_URL + "/{id}", notExistingId)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken()));

        //then
        result.andExpect(status().isNotFound());
    }
}
