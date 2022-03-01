package com.example.answersboxapi.service.impl;

import com.example.answersboxapi.entity.TagEntity;
import com.example.answersboxapi.exceptions.AccessDeniedException;
import com.example.answersboxapi.exceptions.EntityAlreadyProcessedException;
import com.example.answersboxapi.exceptions.EntityNotFoundException;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.tag.TagRequest;
import com.example.answersboxapi.repository.TagRepository;
import com.example.answersboxapi.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.example.answersboxapi.mapper.TagMapper.TAG_MAPPER;
import static com.example.answersboxapi.utils.SecurityUtils.isAdmin;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;

    @Override
    @Transactional
    public Tag create(final TagRequest tagRequest) {
        if (!isAdmin() || existsByName(tagRequest.getName())) {
            throw new AccessDeniedException("Low access to create tag");
        }
        final TagEntity tagEntity = TagEntity.builder()
                .name(tagRequest.getName())
                .build();

        return TAG_MAPPER.toModel(tagRepository.saveAndFlush(tagEntity));
    }

    @Override
    public Tag getById(final UUID id) {
        return TAG_MAPPER.toModel(tagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Tag with id:%s not found", id))));
    }

    @Override
    public boolean existsById(final UUID id) {
        if (!tagRepository.existsById(id)) {
            throw new EntityNotFoundException(String.format("Tag with id:%s not found", id));
        }
        return true;
    }

    @Override
    public void deleteById(final UUID id) {
        getById(id);

        if (!isAdmin()) {
            throw new AccessDeniedException("User can`t delete tag");
        }
        tagRepository.deleteById(id);
    }

    private boolean existsByName(final String name) {
        if (tagRepository.existsByName(name)) {
            throw new EntityAlreadyProcessedException(String.format("Tag: %s already exist", name));
        }
        return false;
    }
}
