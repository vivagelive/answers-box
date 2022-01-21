package com.example.answersboxapi.service;

import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;

import java.util.UUID;

public interface QuestionService {

    Question create(final QuestionRequest questionRequest);

    QuestionEntity getById(final UUID id);
}
