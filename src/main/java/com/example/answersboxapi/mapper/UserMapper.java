package com.example.answersboxapi.mapper;

import com.example.answersboxapi.entity.TagEntity;
import com.example.answersboxapi.entity.UserEntity;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);

    UserEntity toEntity(final User user);

    User toModel(final UserEntity userEntity);

    TagEntity toEntity(final Tag tag);

    Tag toModel(final TagEntity tagEntity);
}
