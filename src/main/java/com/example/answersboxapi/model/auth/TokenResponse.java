package com.example.answersboxapi.model.auth;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TokenResponse {

    private String refreshToken;
    private String accessToken;
    private Instant accessExpirationDate;
    private Instant refreshExpirationDate;
}
