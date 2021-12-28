package com.example.answersboxapi.model.auth;

import com.example.answersboxapi.utils.annotations.Email;
import com.example.answersboxapi.utils.annotations.Password;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @Email
    private String email;

    @Password
    private String password;

    private String firstName;
    private String lastName;
}
