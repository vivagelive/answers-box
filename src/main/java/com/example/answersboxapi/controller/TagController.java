package com.example.answersboxapi.controller;

import com.example.answersboxapi.config.SwaggerConfig;
import com.example.answersboxapi.model.tag.Tag;
import com.example.answersboxapi.model.tag.TagRequest;
import com.example.answersboxapi.service.TagService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    @PostMapping
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "create")
    public ResponseEntity<Tag> create(@RequestBody final TagRequest tagRequest) {
        return new ResponseEntity<>(tagService.create(tagRequest), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "delete tag")
    public ResponseEntity<Tag> deleteTagById(@PathVariable final UUID id) {
        tagService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
