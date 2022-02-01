package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.entity.QuestionDetailsEntity;
import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.repository.QuestionDetailsRepository;
import com.example.answersboxapi.service.QuestionDetailsService;
import com.example.answersboxapi.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.example.answersboxapi.mapper.TagMapper.TAG_MAPPER;

@Service
@RequiredArgsConstructor
public class QuestionDetailsServiceImpl implements QuestionDetailsService {

    private final TagService tagService;

    private final QuestionDetailsRepository questionDetailsRepository;

    @Override
    public void saveQuestionDetails(final QuestionEntity savedQuestion, final UUID tagId) {

        QuestionDetailsEntity questionDetails = QuestionDetailsEntity.builder()
                .questionId(savedQuestion)
                .tagId(TAG_MAPPER.toEntity(tagService.getById(tagId)))
                .build();

        questionDetailsRepository.saveAndFlush(questionDetails);
    }
}
