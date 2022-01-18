package com.example.answersboxapi.model.question;

import com.example.answersboxapi.entity.TagEntity;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class Question {

    private UUID id;
    private Integer rating;
    private String title;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private UUID userId;
    private List<TagEntity> tags;
}
