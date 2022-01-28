package com.example.answersboxapi.controller;

import com.example.answersboxapi.config.SwaggerConfig;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.service.QuestionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.example.answersboxapi.utils.pagination.HeaderUtils.generateHeaders;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "create")
    public ResponseEntity<Question> create(@RequestBody final QuestionRequest questionRequest) {
        return new ResponseEntity<>(questionService.create(questionRequest), HttpStatus.CREATED);
    }

    @GetMapping("/all")
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "getAll")
    public ResponseEntity<List<Question>> getAll(@RequestParam(defaultValue = "1") final int page, @RequestParam(defaultValue = "10") final int size) {
        final Page<Question> foundQuestions = questionService.getAll(page, size);
        final MultiValueMap<String, String> headers = generateHeaders(foundQuestions);

        return new ResponseEntity<>(foundQuestions.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/answers")
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "get answers by id")
    public ResponseEntity<List<Answer>> getByQuestionId(@PathVariable final UUID id) {
        return new ResponseEntity<>(questionService.getAnswersByQuestionId(id), HttpStatus.OK);
    }
}
