package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.exceptions.AccessDeniedException;
import com.example.answersboxapi.exceptions.EntityNotFoundException;
import com.example.answersboxapi.exceptions.InvalidInputDataException;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.user.User;
import com.example.answersboxapi.repository.QuestionRepository;
import com.example.answersboxapi.service.QuestionService;
import com.example.answersboxapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static com.example.answersboxapi.mapper.QuestionMapper.QUESTION_MAPPER;
import static com.example.answersboxapi.mapper.UserMapper.USER_MAPPER;
import static com.example.answersboxapi.utils.SecurityUtils.isAdmin;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;

    private final UserService userService;

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

            return QUESTION_MAPPER.toModel(questionRepository.saveAndFlush(questionEntity));

        } else {
            throw new AccessDeniedException("Admin can`t create a question");
        }
    }

    @Override
    public QuestionEntity getById(final UUID id) {
       return questionRepository.findById(id)
               .orElseThrow(() -> new EntityNotFoundException(String.format("Question with id: %s not found", id)));
    }

    private void checkQuestionFields(final QuestionRequest questionRequest) {
        if (questionRequest.getTitle().isEmpty() || questionRequest.getDescription().isEmpty()) {
            throw new InvalidInputDataException("Empty title or description");
        }
    }
}
