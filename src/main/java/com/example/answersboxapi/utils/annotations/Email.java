package com.example.answersboxapi.utils.annotations;


import com.auth0.jwt.interfaces.Payload;
import com.example.answersboxapi.utils.EmailValidator;

import javax.validation.Constraint;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.example.answersboxapi.utils.constants.Messages.INVALID_EMAIL_MESSAGE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = EmailValidator.class)
public @interface Email {
    String message() default INVALID_EMAIL_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}