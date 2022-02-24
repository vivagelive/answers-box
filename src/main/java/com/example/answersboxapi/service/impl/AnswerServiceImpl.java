package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.exceptions.AccessDeniedException;
import com.example.answersboxapi.exceptions.EntityNotFoundException;
import com.example.answersboxapi.exceptions.InvalidInputDataException;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.answer.AnswerRequest;
import com.example.answersboxapi.model.answer.AnswerUpdateRequest;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.user.User;
import com.example.answersboxapi.repository.AnswerRepository;
import com.example.answersboxapi.service.AnswerService;
import com.example.answersboxapi.service.QuestionService;
import com.example.answersboxapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.example.answersboxapi.mapper.AnswerMapper.ANSWER_MAPPER;
import static com.example.answersboxapi.mapper.QuestionMapper.QUESTION_MAPPER;
import static com.example.answersboxapi.mapper.UserMapper.USER_MAPPER;
import static com.example.answersboxapi.utils.SecurityUtils.hasAccess;
import static com.example.answersboxapi.utils.SecurityUtils.isAdmin;
import static java.lang.String.*;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;

    private final QuestionService questionService;
    private final UserService userService;

    @Override
    @Transactional
    public Answer create(final AnswerRequest answerRequest) {
        final User currentUser = userService.getCurrent();

        checkAnswerText(answerRequest);

        if (isAdmin()) {
            throw new AccessDeniedException("Admin can`t create an answer");
        }
        final Question foundQuestion = questionService.getById(answerRequest.getQuestionId());

        final AnswerEntity answerToSave = AnswerEntity.builder()
                .text(answerRequest.getText())
                .rating(0)
                .createdAt(Instant.now())
                .user(USER_MAPPER.toEntity(currentUser))
                .question(QUESTION_MAPPER.toEntity(foundQuestion))
                .build();

        return ANSWER_MAPPER.toModel(answerRepository.saveAndFlush(answerToSave));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Answer> getAllByQuestionId(final UUID questionId) {
        return ANSWER_MAPPER.toModelList(answerRepository.findAllByQuestionId(questionId, isAdmin()));
    }

    @Override
    @Transactional
    public Answer updateById(final UUID id, final AnswerUpdateRequest answerUpdateRequest) {
        final User currentUser = userService.getCurrent();

        final AnswerEntity foundAnswer = searchAnswer(id);

        if (!hasAccess(foundAnswer.getUser().getId(), currentUser.getId())) {
            throw new AccessDeniedException("Low access to update answer");
        }
        foundAnswer.setText(answerUpdateRequest.getText());
        foundAnswer.setUpdatedAt(Instant.now());

        return ANSWER_MAPPER.toModel(answerRepository.saveAndFlush(foundAnswer));
    }

    @Override
    @Transactional
    public void deleteAllByQuestionId(final UUID questionId) {
        answerRepository.deleteAllByQuestionId(questionId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByQuestionId(final UUID questionId) {
        return answerRepository.existsByQuestionId(questionId);
    }

    @Override
    @Transactional
    public void deleteById(final UUID id) {
        final User currentUser = userService.getCurrent();

        final AnswerEntity foundAnswer = searchAnswer(id);

        if (!hasAccess(foundAnswer.getUser().getId(), currentUser.getId())) {
            throw new AccessDeniedException("Low access to delete answer");
        }
        answerRepository.deleteById(id);
    }

    private void checkAnswerText(final AnswerRequest answerRequest) {
        if (answerRequest.getText().isEmpty()) {
            throw new InvalidInputDataException("Empty answer");
        }
    }

    private AnswerEntity searchAnswer(final UUID answerId) {
        return answerRepository.findById(answerId)
                .orElseThrow(() -> new EntityNotFoundException(format("Answer with id: %s not found", answerId)));
    }
}
