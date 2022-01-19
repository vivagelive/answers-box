package com.example.answersboxapi.service;

import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;

public interface QuestionService {

    Question create(final QuestionRequest questionRequest);
}
