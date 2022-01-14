package com.example.answersboxapi.service;

import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.tag.TagRequest;

public interface TagService {

    Tag create(final TagRequest tagRequest);
}
