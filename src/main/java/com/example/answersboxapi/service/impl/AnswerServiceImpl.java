package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.exceptions.AccessDeniedException;
import com.example.answersboxapi.exceptions.InvalidInputDataException;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.answer.AnswerRequest;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.user.User;
import com.example.answersboxapi.repository.AnswerRepository;
import com.example.answersboxapi.service.AnswerService;
import com.example.answersboxapi.service.QuestionService;
import com.example.answersboxapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.example.answersboxapi.mapper.AnswerMapper.ANSWER_MAPPER;
import static com.example.answersboxapi.mapper.QuestionMapper.QUESTION_MAPPER;
import static com.example.answersboxapi.mapper.UserMapper.USER_MAPPER;
import static com.example.answersboxapi.utils.SecurityUtils.isAdmin;

@Service
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerRepository answerRepository;

    private final QuestionService questionService;
    private final UserService userService;

    @Override
    public Answer create(final AnswerRequest answerRequest) {
        final User currentUser = userService.getCurrent();

        checkAnswerText(answerRequest);

        if (!isAdmin()) {
            final Question foundQuestion = questionService.getById(answerRequest.getQuestionId());

            final AnswerEntity answerToSave = AnswerEntity.builder()
                    .text(answerRequest.getText())
                    .rating(0)
                    .createdAt(Instant.now())
                    .user(USER_MAPPER.toEntity(currentUser))
                    .question(QUESTION_MAPPER.toEntity(foundQuestion))
                    .build();

            return ANSWER_MAPPER.toModel(answerRepository.saveAndFlush(answerToSave));

        } else {
            throw new AccessDeniedException("Admin can`t create an answer");
        }
    }

    @Override
    public List<Answer> getByQuestionId(final UUID questionId) {
        return ANSWER_MAPPER.toModelList(answerRepository.findByQuestionId(questionId, isAdmin()));
    }

    private void checkAnswerText(final AnswerRequest answerRequest) {
        if (answerRequest.getText().isEmpty()) {
            throw new InvalidInputDataException("Empty answer");
        }
    }
}
