package com.example.answersboxapi.service;

import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.answer.AnswerRequest;
import com.example.answersboxapi.model.answer.AnswerUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface AnswerService {

    Answer create(final AnswerRequest answerRequest);

    List<Answer> getByQuestionId(final UUID questionId);

    Answer updateById(final UUID id, final AnswerUpdateRequest answerUpdateRequest);
}
