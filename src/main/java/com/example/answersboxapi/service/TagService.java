package com.example.answersboxapi.service;

import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.tag.TagRequest;

import java.util.UUID;

public interface TagService {

    Tag create(final TagRequest tagRequest);

    Tag getById(final UUID tagId);
}
