package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.entity.TagEntity;
import com.example.answersboxapi.exceptions.AccessDeniedException;
import com.example.answersboxapi.exceptions.EntityAlreadyProcessedException;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.tag.TagRequest;
import com.example.answersboxapi.repository.TagRepository;
import com.example.answersboxapi.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.answersboxapi.mapper.TagMapper.TAG_MAPPER;
import static com.example.answersboxapi.utils.SecurityUtils.isAdmin;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    @Transactional
    public Tag create(final TagRequest tagRequest) {
        if (isAdmin() && !existsByName(tagRequest.getName())) {
            final TagEntity tagEntity = TagEntity.builder()
                    .name(tagRequest.getName())
                    .build();

            return TAG_MAPPER.toModel(tagRepository.saveAndFlush(tagEntity));
        } else {
            throw new AccessDeniedException("Low access to create tag");
        }
    }

    private boolean existsByName(final String name) {
       if (tagRepository.existsByName(name)) {
           throw new EntityAlreadyProcessedException(String.format("Tag: %s already exist", name));
       }
       return false;
    }
}
