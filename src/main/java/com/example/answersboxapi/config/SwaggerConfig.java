package com.example.answersboxapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    public static final String AUTH = "Authorization";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(generateApiInfo())
                .securitySchemes(List.of(securityScheme()))
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.answersboxapi"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo generateApiInfo() {
        return new ApiInfoBuilder()
                .title("Answers-box-api")
                .description("Api allows you to ask some questions")
                .version("1.0")
                .build();
    }

    private SecurityScheme securityScheme()  {
        return new ApiKey(AUTH, "Authorization", "Header");
    }
}
