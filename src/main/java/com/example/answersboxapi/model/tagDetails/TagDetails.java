package com.example.answersboxapi.model.tagDetails;

import lombok.Data;

import java.util.UUID;

@Data
public class TagDetails {

    private UUID id;
    private UUID questionId;
    private UUID tagId;
}
