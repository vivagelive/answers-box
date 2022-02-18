package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.exceptions.AccessDeniedException;
import com.example.answersboxapi.exceptions.EntityNotFoundException;
import com.example.answersboxapi.exceptions.InvalidInputDataException;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionDetails;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.question.QuestionUpdateRequest;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.user.User;
import com.example.answersboxapi.repository.QuestionRepository;
import com.example.answersboxapi.service.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.example.answersboxapi.mapper.QuestionMapper.QUESTION_MAPPER;
import static com.example.answersboxapi.mapper.UserMapper.USER_MAPPER;
import static com.example.answersboxapi.utils.SecurityUtils.checkAccess;
import static com.example.answersboxapi.utils.SecurityUtils.isAdmin;
import static com.example.answersboxapi.utils.pagination.PagingUtils.toPageRequest;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    private final AnswerService answerService;
    private final TagService tagService;
    private final QuestionDetailsService questionsDetailsService;
    private final UserService userService;

    public QuestionServiceImpl(final QuestionRepository questionRepository,
                               final UserService userService,
                               final TagService tagService,
                               final QuestionDetailsService questionDetailsService,
                               @Lazy final AnswerService answerService) {
        this.questionRepository = questionRepository;
        this.userService = userService;
        this.answerService = answerService;
        this.tagService = tagService;
        this.questionsDetailsService = questionDetailsService;
    }

    @Override
    public Question create(final QuestionRequest questionRequest) {
        final User currentUser = userService.getCurrent();

        checkQuestionFields(questionRequest.getTitle(), questionRequest.getDescription());

        if (!isAdmin()) {
            final QuestionEntity questionEntity = QuestionEntity.builder()
                    .rating(0)
                    .title(questionRequest.getTitle())
                    .description(questionRequest.getDescription())
                    .user(USER_MAPPER.toEntity(currentUser))
                    .createdAt(Instant.now())
                    .build();

            final QuestionEntity savedQuestion = questionRepository.saveAndFlush(questionEntity);

            return QUESTION_MAPPER.toModel(savedQuestion);

        } else {
            throw new AccessDeniedException("Admin can`t create a question");
        }
    }

    @Override
    public Question getById(final UUID id) {
        return QUESTION_MAPPER.toModel(questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Question with id: %s not found", id))));
    }

    @Override
    public Page<Question> getAll(final int page, final int size, final List<UUID> tagIds) {
        return questionRepository.findAll(toPageRequest(page, size), tagIds, isAdmin()).map(QUESTION_MAPPER::toModel);
    }

    @Override
    public List<Answer> getAnswersByQuestionId(final UUID questionId) {
        return answerService.getByQuestionId(questionId);
    }

    @Override
    @Transactional
    public Question addTagToQuestion(final UUID questionId, final UUID tagId) {
        final Question foundQuestion = getById(questionId);

        final Tag foundTag = tagService.getById(tagId);

        foundQuestion.getTagsIds().add(foundTag.getId());

        questionsDetailsService.create(foundQuestion, foundTag);
        return foundQuestion;
    }

    @Override
    @Transactional
    public Question removeTagFromQuestion(final UUID questionId, final UUID tagId) {
        final Question foundQuestion = getById(questionId);

        final List<QuestionDetails> foundDetails = questionsDetailsService.getAllByQuestionId(questionId);

        if (tagExistsById(tagId)) {
            foundDetails.forEach(questionDetails -> {
                if (questionDetails.getTagId().equals(tagId)) {
                    foundQuestion.getTagsIds().remove(tagId);

                    questionsDetailsService.deleteById(questionDetails.getId());
                }
            });
        }
        return foundQuestion;
    }

    @Override
    public Question updateById(final UUID id, final QuestionUpdateRequest questionUpdateRequest) {
        final User currentUser = userService.getCurrent();

        final QuestionEntity foundQuestion = QUESTION_MAPPER.toEntity(getById(id));

        if (checkAccess(foundQuestion.getUser().getId(), currentUser.getId())) {

            checkQuestionFields(questionUpdateRequest.getTitle(), questionUpdateRequest.getDescription());

            foundQuestion.setUpdatedAt(Instant.now());
            foundQuestion.setTitle(questionUpdateRequest.getTitle());
            foundQuestion.setDescription(questionUpdateRequest.getDescription());

            return QUESTION_MAPPER.toModel(questionRepository.saveAndFlush(foundQuestion));
        }
        throw new AccessDeniedException("Low access to update question");
    }

    private void checkQuestionFields(final String title, final String description) {
        if (title.isEmpty() || description.isEmpty()) {
            throw new InvalidInputDataException("Empty title or description");
        }
    }

    private boolean tagExistsById(final UUID id) {
        return tagService.existsById(id);
    }
}
