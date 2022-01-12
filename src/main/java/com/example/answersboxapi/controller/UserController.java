package com.example.answersboxapi.controller;

import com.example.answersboxapi.config.SwaggerConfig;
import com.example.answersboxapi.model.User;
import com.example.answersboxapi.service.UserService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @DeleteMapping("/{id}")
    @ApiOperation(authorizations = @Authorization(value = SwaggerConfig.AUTH), value = "deleteById")
    public ResponseEntity<Void> deleteById(@PathVariable final UUID id) {
        userService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
