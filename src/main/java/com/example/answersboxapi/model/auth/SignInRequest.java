package com.example.answersboxapi.model.auth;

import com.example.answersboxapi.utils.annotations.Email;
import com.example.answersboxapi.utils.annotations.Password;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignInRequest {

    @Email
    private String email;

    @Password
    private String password;
}
