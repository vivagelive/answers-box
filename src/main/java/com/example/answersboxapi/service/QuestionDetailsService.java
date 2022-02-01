package com.example.answersboxapi.service;

import com.example.answersboxapi.entity.QuestionEntity;

import java.util.UUID;

public interface QuestionDetailsService {

    void saveQuestionDetails(final QuestionEntity question, final UUID tagId);
}
