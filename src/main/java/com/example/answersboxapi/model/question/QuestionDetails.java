package com.example.answersboxapi.model.question;

import lombok.Data;

import java.util.UUID;

@Data
public class QuestionDetails {

    private UUID id;
    private UUID questionId;
    private UUID tagId;
}
