package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.entity.QuestionDetailsEntity;
import com.example.answersboxapi.exceptions.EntityNotFoundException;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionDetails;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.repository.QuestionDetailsRepository;
import com.example.answersboxapi.service.QuestionDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.example.answersboxapi.mapper.QuestionDetailsMapper.QUESTION_DETAILS_MAPPER;
import static com.example.answersboxapi.mapper.QuestionMapper.QUESTION_MAPPER;
import static com.example.answersboxapi.mapper.TagMapper.TAG_MAPPER;
import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class QuestionDetailsServiceImpl implements QuestionDetailsService {

    private final QuestionDetailsRepository questionDetailsRepository;

    @Override
    public QuestionDetails create(final Question question, final Tag tag) {
        final QuestionDetailsEntity questionDetailsToSave = QuestionDetailsEntity.builder()
                .questionId(QUESTION_MAPPER.toEntity(question))
                .tagId(TAG_MAPPER.toEntity(tag))
                .build();

        return QUESTION_DETAILS_MAPPER.toModel(questionDetailsRepository.saveAndFlush(questionDetailsToSave));
    }

    @Override
    public QuestionDetails getById(final UUID questionId) {
        return QUESTION_DETAILS_MAPPER.toModel(
                questionDetailsRepository.findByQuestionId(questionId)
                        .orElseThrow(() -> new EntityNotFoundException(format("Question details with id: %s not found", questionId))));
    }

    @Override
    @Transactional
    public void delete(final UUID questionDetailsId) {
        questionDetailsRepository.deleteById(questionDetailsId);
    }
}
