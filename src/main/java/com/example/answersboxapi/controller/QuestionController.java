package com.example.answersboxapi.controller;

import com.example.answersboxapi.config.SwaggerConfig;
import com.example.answersboxapi.model.answer.Answer;
import com.example.answersboxapi.model.question.Question;
import com.example.answersboxapi.model.question.QuestionRequest;
import com.example.answersboxapi.model.question.QuestionUpdateRequest;
import com.example.answersboxapi.service.QuestionService;
import com.example.answersboxapi.model.SortParams;
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
    public ResponseEntity<List<Question>> getAll(@RequestParam(defaultValue = "1") final int page,
                                                 @RequestParam(defaultValue = "10") final int size,
                                                 @RequestParam final List<UUID> tagIds,
                                                 @RequestParam(defaultValue = "-createdAt") final SortParams sortParams,
                                                 @RequestParam(defaultValue = "", required = false) final String searchParam,
                                                 @RequestParam(required = false, defaultValue = "false") final Boolean isDeleted) {
        final Page<Question> foundQuestions = questionService.getAll(page, size, tagIds, sortParams, searchParam, isDeleted);
        final MultiValueMap<String, String> headers = generateHeaders(foundQuestions);

        return new ResponseEntity<>(foundQuestions.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/{id}/answers")
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "get answers by id")
    public ResponseEntity<List<Answer>> getAnswersByQuestionId(@PathVariable final UUID id,
                                                               @RequestParam(defaultValue = "1") final int page,
                                                               @RequestParam(defaultValue = "10") final int size,
                                                               @RequestParam(defaultValue = "-createdAt") final SortParams sortParams,
                                                               @RequestParam(defaultValue = "", required = false) final String searchParam,
                                                               @RequestParam(defaultValue = "false", required = false) final Boolean isDeleted) {
        final Page<Answer> answers = questionService.getAnswersByQuestionId(id, page, size, sortParams, searchParam, isDeleted);
        final MultiValueMap<String, String> headers = generateHeaders(answers);

        return new ResponseEntity<>(answers.getContent(), headers, HttpStatus.OK);
    }

    @PutMapping("/{questionId}/add-tag/{tagId}")
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "add tag by question id")
    public ResponseEntity<Question> addTagToQuestion(@PathVariable final UUID questionId, @PathVariable final UUID tagId) {
        return new ResponseEntity<>(questionService.addTagToQuestion(questionId, tagId), HttpStatus.OK);
    }

    @PutMapping("/{questionId}/remove-tag/{tagId}")
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "remove tag by question id")
    public ResponseEntity<Question> removeTagFromQuestion(@PathVariable final UUID questionId, @PathVariable final UUID tagId) {
        return new ResponseEntity<>(questionService.removeTagFromQuestion(questionId, tagId), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "update by question id")
    public ResponseEntity<Question> update(@PathVariable final UUID id, @RequestBody final QuestionUpdateRequest questionUpdateRequest) {
        return new ResponseEntity<>(questionService.updateById(id, questionUpdateRequest), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "delete by question id")
    public ResponseEntity<Void> deleteById(@PathVariable final UUID id) {
        questionService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/{id}/increase-rating")
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "increase question rating")
    public ResponseEntity<Question> increaseRating(@PathVariable final UUID id) {
        return new ResponseEntity<>(questionService.increaseRatingById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}/decrease-rating")
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "decrease question rating")
    public ResponseEntity<Question> decreaseRating(@PathVariable final UUID id) {
        return new ResponseEntity<>(questionService.decreaseRatingById(id), HttpStatus.OK);
    }
}
