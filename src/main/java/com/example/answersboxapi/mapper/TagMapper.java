package com.example.answersboxapi.mapper;

import com.example.answersboxapi.entity.TagEntity;
import com.example.answersboxapi.model.tag.Tag;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TagMapper {

    TagMapper TAG_MAPPER = Mappers.getMapper(TagMapper.class);

    TagEntity toEntity(final Tag tag);

    Tag toModel(final TagEntity tagEntity);
}
