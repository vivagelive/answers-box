package com.example.answersboxapi.mapper;

import com.example.answersboxapi.entity.AnswerEntity;
import com.example.answersboxapi.model.answer.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    AnswerMapper ANSWER_MAPPER = Mappers.getMapper(AnswerMapper.class);

    @Mapping(target = "user.id", source = "userId")
    @Mapping(target = "question.id", source = "questionId")
    AnswerEntity toEntity(final Answer answer);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "questionId", expression = "java(answerEntity.getQuestion().getId())")
    Answer toModel(final AnswerEntity answerEntity);
}
