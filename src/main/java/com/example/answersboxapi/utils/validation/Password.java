package com.example.answersboxapi.utils.validation;

import com.example.answersboxapi.utils.PasswordValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.example.answersboxapi.utils.constants.Messages.INVALID_PASSWORD_MESSAGE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface Password {
    String message() default INVALID_PASSWORD_MESSAGE;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
