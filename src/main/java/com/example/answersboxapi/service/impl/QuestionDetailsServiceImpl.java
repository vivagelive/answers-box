package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.entity.QuestionDetailsEntity;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionDetails;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.repository.QuestionDetailsRepository;
import com.example.answersboxapi.service.QuestionDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.example.answersboxapi.mapper.QuestionDetailsMapper.QUESTION_DETAILS_MAPPER;
import static com.example.answersboxapi.mapper.QuestionMapper.QUESTION_MAPPER;
import static com.example.answersboxapi.mapper.TagMapper.TAG_MAPPER;

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
    public List<QuestionDetails> getAllByQuestionId(final UUID id) {
        return QUESTION_DETAILS_MAPPER.toModelList(
                questionDetailsRepository.findAllByQuestionId(id));
    }

    @Override
    @Transactional
    public void deleteById(final UUID id) {
        questionDetailsRepository.deleteById(id);
    }
}
