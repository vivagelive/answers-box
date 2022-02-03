package com.example.answersboxapi.service;

import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionDetails;
import com.example.answersboxapi.model.tag.Tag;

import java.util.UUID;

public interface QuestionDetailsService {

    QuestionDetails create(final Question question, final Tag tag);

    QuestionDetails getById(final UUID questionId);

    void delete(final UUID questionDetailsId);
}
