package com.example.answersboxapi.service;

import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.answer.AnswerRequest;

import java.util.List;
import java.util.UUID;

public interface AnswerService {

    Answer create(final AnswerRequest answerRequest);

    List<Answer> getByQuestionId(final UUID questionId);
}
