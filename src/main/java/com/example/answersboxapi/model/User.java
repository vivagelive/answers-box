package com.example.answersboxapi.model;

import com.example.answersboxapi.enums.UserRole;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class User {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private UserRole role;
}
