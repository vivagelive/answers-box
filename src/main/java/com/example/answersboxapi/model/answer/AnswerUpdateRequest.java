package com.example.answersboxapi.model.answer;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AnswerUpdateRequest {

    private UUID answerId;
    private String text;
}
