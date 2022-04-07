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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;

import static com.example.answersboxapi.enums.UserEntityRole.ROLE_ADMIN;
import static com.example.answersboxapi.enums.UserEntityRole.ROLE_USER;
import static com.example.answersboxapi.utils.GeneratorUtil.generateSignUpRequest;
import static com.example.answersboxapi.utils.GeneratorUtil.generateTagRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TagControllerTest extends AbstractIntegrationTest {

    @ParameterizedTest
    @MethodSource("createWithStatusesAndRoles")
    public void createTag(final ResultMatcher status, final UserEntityRole role, final boolean happyCondition) throws Exception {
        //given
        final TagRequest tagRequest = generateTagRequest();

        final SignUpRequest signUpRequest = generateSignUpRequest();
        insertUserOrAdmin(signUpRequest, role);

        final TokenResponse token = createSignIn(signUpRequest);

        //when
        final MvcResult result = mockMvc.perform(post(TAG_URL)
                .header(AUTHORIZATION, TOKEN_PREFIX + token.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(tagRequest)))
                .andExpect(status)
                .andReturn();

        //then
        assertCondition(happyCondition, result, tagRequest);
    }

    @ParameterizedTest
    @MethodSource("httpMethodsWithUrls")
    public void tagEndpoints_whenNotSignedIn(final HttpMethod method, final String url) throws Exception {
        //given & when
        final ResultActions result = mockMvc.perform(request(method, TAG_URL + url, savedTag.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(tagRequest)));

        //then
        result.andExpect(status().isUnauthorized());
    }

    @Test
    public void createTag_whenDuplicate() throws Exception {
        //given
        final SignUpRequest adminRequest = generateSignUpRequest();
        insertUserOrAdmin(adminRequest, ROLE_ADMIN);

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
    public void deleteTag(final ResultMatcher status, final UserEntityRole role, UUID id) throws Exception {
        //given
        final SignUpRequest signUpRequest = generateSignUpRequest();

        final SignUpRequest adminsRequest = generateSignUpRequest();
        insertUserOrAdmin(adminsRequest, ROLE_ADMIN);
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

    static Stream<Arguments> deleteWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isForbidden(), ROLE_USER, null),
                arguments(status().isNoContent(), ROLE_ADMIN, null),
                arguments(status().isNotFound(), ROLE_ADMIN, UUID.randomUUID()));
    }

    static Stream<Arguments> createWithStatusesAndRoles() {
        return Stream.of(
                arguments(status().isForbidden(), ROLE_USER, false),
                arguments(status().isCreated(), ROLE_ADMIN, true));
    }

    static Stream<Arguments> httpMethodsWithUrls() {
        return Stream.of(
                arguments(HttpMethod.POST, ""),
                arguments(HttpMethod.DELETE, "/{id}"));
    }

    private void assertCondition(final boolean happyCondition, final MvcResult result, final TagRequest tagRequest) throws IOException {
        if (happyCondition){
            final Tag foundTag = objectMapper.readValue(result.getResponse().getContentAsByteArray(), Tag.class);

            assertEquals(foundTag.getName(), tagRequest.getName());
        }
    }
}
