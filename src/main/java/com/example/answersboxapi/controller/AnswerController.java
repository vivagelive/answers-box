package com.example.answersboxapi.controller;

import com.example.answersboxapi.config.SwaggerConfig;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.answer.AnswerRequest;
import com.example.answersboxapi.model.answer.AnswerUpdateRequest;
import com.example.answersboxapi.service.AnswerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/answers")
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "create")
    public ResponseEntity<Answer> create(@RequestBody final AnswerRequest answerRequest) {
        return new ResponseEntity<>(answerService.create(answerRequest), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "update answer")
    public ResponseEntity<Answer> update(@PathVariable final UUID id, @RequestBody final AnswerUpdateRequest answerUpdateRequest) {
        return new ResponseEntity<>(answerService.updateById(id, answerUpdateRequest), HttpStatus.OK);
    }
}
