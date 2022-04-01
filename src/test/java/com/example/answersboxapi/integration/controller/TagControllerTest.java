package com.example.answersboxapi.integration.controller;

import com.example.answersboxapi.entity.TagEntity;
import com.example.answersboxapi.enums.UserEntityRole;
import com.example.answersboxapi.integration.AbstractIntegrationTest;
import com.example.answersboxapi.model.auth.SignUpRequest;
import com.example.answersboxapi.model.auth.TokenResponse;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.tag.TagRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.UUID;
import java.util.stream.Stream;

import static com.example.answersboxapi.enums.UserEntityRole.ROLE_ADMIN;
import static com.example.answersboxapi.enums.UserEntityRole.ROLE_USER;
import static com.example.answersboxapi.utils.GeneratorUtil.generateSignUpRequest;
import static com.example.answersboxapi.utils.GeneratorUtil.generateTagRequest;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TagControllerTest extends AbstractIntegrationTest {

    @ParameterizedTest
    @MethodSource("createWithStatusesAndRoles")
    public void create_withUserAndAdminsAccess(final ResultMatcher status, final UserEntityRole role) throws Exception {
        //given
        final TagRequest tagRequest = generateTagRequest();

        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUserOrAdmin(signUpRequest, role);

        final TokenResponse token = createSignIn(signUpRequest);

        //when
        final ResultActions result = mockMvc.perform(post(TAG_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(tagRequest)));

        //then
        result.andExpect(status);
    }

    @Test
    public void create_whenNotSignedIn() throws Exception {
        //given & when
        final ResultActions result = mockMvc.perform(post(TAG_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(tagRequest)));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void create_whenDuplicate() throws Exception {
        //given
        final SignUpRequest adminRequest = generateSignUpRequest();
        insertAdmin(adminRequest);

        final TokenResponse adminToken = createSignIn(adminRequest);

        //when
        createTag(adminToken, tagRequest);

        final ResultActions result = mockMvc.perform(post(TAG_URL)
                        .header(AUTHORIZATION, TOKEN_PREFIX + adminToken.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(tagRequest)));

        //then
        result.andExpect(status().isUnprocessableEntity());
    }

    @ParameterizedTest
    @MethodSource("deleteWithStatusesAndRoles")
    public void deleteById_withUserAndAdminsAccess(final ResultMatcher status, final UserEntityRole role, UUID id) throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();

        SignUpRequest adminsRequest = generateSignUpRequest();
        insertAdmin(adminsRequest);
        final TokenResponse adminsToken = createSignIn(adminsRequest);
        final Tag savedTag = createTag(adminsToken, tagRequest);

        insertUserOrAdmin(signUpRequest, role);

        final TokenResponse activeUser = createSignIn(signUpRequest);

        final UUID idForSearch = checkIdForSearch(id, savedTag.getId());

        //when
        mockMvc.perform(delete(TAG_URL + "/{id}", idForSearch)
                .header(AUTHORIZATION, TOKEN_PREFIX + activeUser.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andReturn();

        final TagEntity foundTag = tagRepository.getById(savedTag.getId());

        //then
        assertNotNull(foundTag);
    }

    @Test
    public void deleteById_whenNotSignedIn() throws Exception {
        //given
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

    static Stream<Arguments> deleteWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isForbidden(), ROLE_USER, null),
                arguments(status().isNoContent(), ROLE_ADMIN, null),
                arguments(status().isNotFound(), ROLE_ADMIN, UUID.randomUUID()));
    }

    static Stream<Arguments> createWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isForbidden(), ROLE_USER),
                arguments(status().isCreated(), ROLE_ADMIN));
    }
}
