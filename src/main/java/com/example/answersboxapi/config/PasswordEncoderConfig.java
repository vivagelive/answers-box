package com.example.answersboxapi.config;

import com.auth0.jwt.algorithms.Algorithm;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;

@Data
@Configuration
public class PasswordEncoderConfig {

    private Algorithm algorithm;

    @Value("${jwt:secret}")
    private String secret;

    @PostConstruct
    private void init(){ algorithm = Algorithm.HMAC256(secret.getBytes());}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
