package com.example.answersboxapi.mapper;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.entity.QuestionDetailsEntity;
import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.entity.TagEntity;
import com.example.answersboxapi.model.question.Question;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    QuestionMapper QUESTION_MAPPER = Mappers.getMapper(QuestionMapper.class);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "questionDetails", ignore = true)
    @Mapping(target = "answers", ignore = true)
    QuestionEntity toEntity(final Question question);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "tagsIds", expression = "java(questionsDetailsToIds(questionEntity.getQuestionDetails()))")
    @Mapping(target = "answerIds", expression = "java(answersToIds(questionEntity.getAnswers()))")
    Question toModel(final QuestionEntity questionEntity);

    default List<UUID> questionsDetailsToIds(final List<QuestionDetailsEntity> questionDetails) {
        if (questionDetails != null) {
            return questionDetails.stream()
                    .map(QuestionDetailsEntity::getTagId)
                    .map(TagEntity::getId)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    default List<UUID> answersToIds(final List<AnswerEntity> answers) {
        if (answers != null) {
            return answers.stream().map(AnswerEntity::getId).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
