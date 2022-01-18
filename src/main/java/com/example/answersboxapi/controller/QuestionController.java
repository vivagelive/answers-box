package com.example.answersboxapi.controller;

import com.example.answersboxapi.config.SwaggerConfig;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.service.QuestionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/question")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "create")
    public ResponseEntity<Question> create(@RequestBody final QuestionRequest questionRequest) {
        return new ResponseEntity<>(questionService.create(questionRequest), HttpStatus.CREATED);
    }
}
