package com.example.answersboxapi.mapper;

import com.example.answersboxapi.entity.TagDetailsEntity;
import com.example.answersboxapi.model.tagDetails.TagDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TagDetailsMapper {

    TagDetailsMapper TAG_DETAILS_MAPPER = Mappers.getMapper(TagDetailsMapper.class);

    @Mapping(source = "questionId", target = "questionId.id")
    @Mapping(source = "tagId", target = "tagId.id")
    TagDetailsEntity toEntity(final TagDetails tagDetailsEntityDto);
}
