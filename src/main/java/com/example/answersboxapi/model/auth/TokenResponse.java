package com.example.answersboxapi.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private String refreshToken;
    private String accessToken;
    private Instant accessExpirationDate;
    private Instant refreshExpirationDate;
}
