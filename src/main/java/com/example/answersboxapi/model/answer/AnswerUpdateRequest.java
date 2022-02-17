package com.example.answersboxapi.model.answer;

import lombok.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnswerUpdateRequest {

    private String text;
}
