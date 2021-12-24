package com.example.answersboxapi.utils;

import com.example.answersboxapi.utils.validation.Email;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<Email, String> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-z0-9._]+@[a-z0-9.-]+\\.[a-z]{2,6}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        final Matcher matcher = EMAIL_PATTERN.matcher(value);
        return matcher.matches();
    }
}