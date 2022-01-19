package com.example.answersboxapi.mapper;

import com.example.answersboxapi.entity.QuestionEntity;
import com.example.answersboxapi.entity.UserEntity;
import com.example.answersboxapi.model.user.User;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper USER_MAPPER = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "questions", ignore = true)
    UserEntity toEntity(final User user);

    @Mapping(target = "questions", expression = "java(questionsToIds(userEntity.getQuestions()))")
    User toModel(final UserEntity userEntity);

    default List<UUID> questionsToIds(final List<QuestionEntity> questions) {
        if (questions != null) {
            return questions.stream().map(QuestionEntity::getId).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
