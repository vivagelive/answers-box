package com.example.answersboxapi.mapper;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.entity.QuestionDetailsEntity;
import com.example.answersboxapi.model.answer.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    AnswerMapper ANSWER_MAPPER = Mappers.getMapper(AnswerMapper.class);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "questionDetails", ignore = true)
    AnswerEntity toEntity(final Answer answer);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "questionDetails", expression = "java(questionDetailsToIds(answerEntity.getQuestionDetails()))")
    Answer toModel(final AnswerEntity answerEntity);

    default List<UUID> questionDetailsToIds(final List<QuestionDetailsEntity> questionDetails) {
        if (questionDetails != null){
            return questionDetails.stream().map(QuestionDetailsEntity::getId).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
