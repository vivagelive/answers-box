package com.example.answersboxapi.service;

import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.question.QuestionUpdateRequest;
import com.example.answersboxapi.utils.sorting.SortingParams;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface QuestionService {

    Question create(final QuestionRequest questionRequest);

    Question getById(final UUID id);

    Page<Question> getAll(final int page, final int size, final List<UUID> tagIds, final SortingParams sortParams,
                          final String searchParam, final boolean isDeleted);

    Page<Answer> getAnswersByQuestionId(final UUID id, final SortingParams sortParams,
                                        final String searchParam, final boolean isDeleted, final int page, final int size);

    Question addTagToQuestion(final UUID questionId, final UUID tagId);

    Question removeTagFromQuestion(final UUID questionId, final UUID tagId);

    Question updateById(final UUID id, final QuestionUpdateRequest questionUpdateRequest);

    void deleteById(final UUID id);

    Question increaseRatingById(final UUID id);

    Question decreaseRatingById(final UUID id);
}
