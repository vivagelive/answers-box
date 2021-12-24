package com.example.answersboxapi.model.auth;

import com.example.answersboxapi.utils.validation.Email;
import com.example.answersboxapi.utils.validation.Password;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpRequest {

    @Email
    private String email;

    @Password
    private String password;

    private String firstName;
    private String lastName;
}
