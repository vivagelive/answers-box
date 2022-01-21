package com.example.answersboxapi.model.answer;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class Answer {

    private UUID id;
    private String text;
    private Integer rating;
    private UUID userId;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private UUID questionId;
}
