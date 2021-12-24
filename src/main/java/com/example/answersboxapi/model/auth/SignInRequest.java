package com.example.answersboxapi.model.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignInRequest {

    private String email;
    private String password;
}
