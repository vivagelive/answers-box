package com.example.answersboxapi.unit;

import com.example.answersboxapi.controller.AuthController;
import com.example.answersboxapi.exceptions.ExceptionHandling;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AbstractUnitTest {

    private static final Faker FAKER = new Faker();

    protected static final String INVALID_EMAIL = FAKER.random().hex(25);
    protected static final String INVALID_PASSWORD = FAKER.internet().password(false);

    protected static final String VALID_EMAIL = FAKER.bothify("???##@gmail.com");
    protected static final String VALID_PASSWORD = FAKER.bothify("????####");

    @InjectMocks
    protected AuthController authController;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new ExceptionHandling()).build();
    }
}
