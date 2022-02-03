package com.example.answersboxapi.service;

import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionDetails;
import com.example.answersboxapi.model.tag.Tag;

import java.util.List;
import java.util.UUID;

public interface QuestionDetailsService {

    QuestionDetails create(final Question question, final Tag tag);

    List<QuestionDetails> getAllByQuestionId(final UUID questionId);

    void deleteById(final UUID id);
}
