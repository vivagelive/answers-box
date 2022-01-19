package com.example.answersboxapi.mapper;

import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.entity.TagDetailsEntity;
import com.example.answersboxapi.model.question.Question;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    QuestionMapper QUESTION_MAPPER = Mappers.getMapper(QuestionMapper.class);

    QuestionEntity toEntity(final Question questionEntityDto);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "tagDetailIds", expression = "java(tagDetailsToIds(questionEntity.getTagDetails()))")
    Question toModel(final QuestionEntity questionEntity);

    default List<UUID> tagDetailsToIds(final List<TagDetailsEntity> tagDetails) {
        return tagDetails.stream().map(TagDetailsEntity::getId).collect(Collectors.toList());
    }
}
