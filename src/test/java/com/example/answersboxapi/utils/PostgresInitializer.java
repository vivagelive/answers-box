package com.example.answersboxapi.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class PostgresInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String POSTGRES_USER = "postgres";
    private static final String POSTGRES_PASSWORD = "root";

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:11")
            .withDatabaseName("userapi")
            .withUsername("postgres")
            .withPassword("root");

    static {
        POSTGRESQL_CONTAINER.start();
    }

    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        applyProperties(applicationContext);
    }

    private void applyProperties(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url:" + POSTGRESQL_CONTAINER.getJdbcUrl(),
                "spring.datasource.username:" + POSTGRES_USER,
                "spring.datasource.password:" + POSTGRES_PASSWORD
        ).applyTo(applicationContext);
    }
}
