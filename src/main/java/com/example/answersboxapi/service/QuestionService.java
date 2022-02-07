package com.example.answersboxapi.service;

import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface QuestionService {

    Question create(final QuestionRequest questionRequest);

    Question getById(final UUID id);

    Page<Question> getAll(final int page, final int size, final List<UUID> tagIds);

    List<Answer> getAnswersByQuestionId(final UUID questionId);

    Question addTagToQuestion(final UUID questionId, final UUID tagId);

    Question removeTagFromQuestion(final UUID questionId, final UUID tagId);
}
