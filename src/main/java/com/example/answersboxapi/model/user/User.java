package com.example.answersboxapi.model.user;

import com.example.answersboxapi.enums.UserRole;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class User implements Serializable {

    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
    private UserRole role;
    private List<UUID> questions;
}
