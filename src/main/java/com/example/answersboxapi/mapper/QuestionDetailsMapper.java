package com.example.answersboxapi.mapper;

import com.example.answersboxapi.entity.QuestionDetailsEntity;
import com.example.answersboxapi.model.question.QuestionDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface QuestionDetailsMapper {

    QuestionDetailsMapper QUESTION_DETAILS_MAPPER = Mappers.getMapper(QuestionDetailsMapper.class);

    @Mapping(source = "questionId", target = "questionId.id")
    @Mapping(source = "tagId", target = "tagId.id")
    QuestionDetailsEntity toEntity(final QuestionDetails questionDetails);

    @Mapping(source = "questionId.id", target = "questionId")
    @Mapping(source = "tagId.id", target = "tagId")
    QuestionDetails toModel(final QuestionDetailsEntity questionDetails);

    List<QuestionDetails> toModelList(final List<QuestionDetailsEntity> questionDetailsEntities);
}
