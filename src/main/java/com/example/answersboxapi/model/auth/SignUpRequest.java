package com.example.answersboxapi.model.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpRequest {

    private String email;
    private String password;
    private String firstName;
    private String lastName;
}
