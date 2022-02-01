package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.exceptions.AccessDeniedException;
import com.example.answersboxapi.exceptions.EntityNotFoundException;
import com.example.answersboxapi.exceptions.InvalidInputDataException;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.user.User;
import com.example.answersboxapi.repository.QuestionRepository;
import com.example.answersboxapi.service.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.answersboxapi.mapper.QuestionMapper.QUESTION_MAPPER;
import static com.example.answersboxapi.mapper.UserMapper.USER_MAPPER;
import static com.example.answersboxapi.utils.SecurityUtils.isAdmin;
import static com.example.answersboxapi.utils.pagination.PagingUtils.toPageRequest;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    private final AnswerService answerService;
    private final TagService tagService;
    private final QuestionDetailsService questionDetailsService;
    private final UserService userService;


    public QuestionServiceImpl(final QuestionRepository questionRepository,
                               final UserService userService,
                               final QuestionDetailsService questionDetailsService,
                               final TagService tagService,
                               @Lazy final AnswerService answerService) {
        this.questionRepository = questionRepository;
        this.userService = userService;
        this.answerService = answerService;
        this.questionDetailsService = questionDetailsService;
        this.tagService = tagService;
    }

    @Override
    public Question create(final QuestionRequest questionRequest) {
        final User currentUser = userService.getCurrent();

        checkQuestionFields(questionRequest);

        if (!isAdmin()) {
            final QuestionEntity questionEntity = QuestionEntity.builder()
                    .rating(0)
                    .title(questionRequest.getTitle())
                    .description(questionRequest.getDescription())
                    .user(USER_MAPPER.toEntity(currentUser))
                    .createdAt(Instant.now())
                    .build();

            final QuestionEntity savedQuestion = questionRepository.saveAndFlush(questionEntity);

            questionDetailsService.saveQuestionDetails(savedQuestion, questionRequest.getTagId());

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
    public Page<Question> getAll(final int page, final int size) {
        return questionRepository.findAll(toPageRequest(page, size), isAdmin()).map(QUESTION_MAPPER::toModel);
    }

    @Override
    public List<Answer> getAnswersByQuestionId(final UUID questionId) {
        return answerService.getByQuestionId(questionId);
    }

    @Override
    public Page<Question> getAllFilteredByTagId(final int page, final int size, final UUID tagId) {
        if (tagService.getById(tagId) != null) {
            final Page<Question> foundQuestions = getAll(page, size);

            final List<Question> filteredQuestions = foundQuestions.getContent().stream()
                    .filter(question -> question.getTagsIds()
                            .stream().anyMatch(uuid -> uuid.equals(tagId))).
                    collect(Collectors.toList());

            return new PageImpl<>(filteredQuestions, toPageRequest(page, size), filteredQuestions.size());
        }
        return null;
    }

    private void checkQuestionFields(final QuestionRequest questionRequest) {
        if (questionRequest.getTitle().isEmpty() || questionRequest.getDescription().isEmpty()) {
            throw new InvalidInputDataException("Empty title or description");
        }
    }
}
