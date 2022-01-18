package com.example.answersboxapi.mapper;

import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.entity.TagDetailsEntity;
import com.example.answersboxapi.entity.TagEntity;
import com.example.answersboxapi.model.tagDetails.TagDetails;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TagDetailsMapper {

    TagDetailsMapper TAG_DETAILS_MAPPER = Mappers.getMapper(TagDetailsMapper.class);

    TagDetails toModel(final TagDetailsEntity tagDetailsEntity);

    default UUID fromEntity(final QuestionEntity questionEntity) {
        if (questionEntity != null) {
            return questionEntity.getId();
        } else {
            return null;
        }
    }

    default UUID fromEntity(final TagEntity tagEntity) {
        if (tagEntity != null) {
            return tagEntity.getId();
        } else {
            return null;
        }
    }
}
