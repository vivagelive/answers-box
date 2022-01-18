package com.example.answersboxapi.mapper;

import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.model.question.Question;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    QuestionMapper QUESTION_MAPPER = Mappers.getMapper(QuestionMapper.class);

    QuestionEntity toEntity(final Question question);

    @Mapping(target = "userId", source = "user.id")
    Question toModel(final QuestionEntity questionEntity);
}
