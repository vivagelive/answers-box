package com.example.answersboxapi.service;

import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.answer.AnswerRequest;

public interface AnswerService {

    Answer create(final AnswerRequest answerRequest);
}
