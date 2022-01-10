package com.example.answersboxapi.utils;

import com.example.answersboxapi.utils.annotations.Password;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?=.*\\d)[\\w]{6,}");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        final Matcher matcher = PASSWORD_PATTERN.matcher(value);
        return matcher.matches();
    }
}
