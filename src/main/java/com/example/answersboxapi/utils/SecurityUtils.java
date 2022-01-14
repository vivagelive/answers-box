package com.example.answersboxapi.utils;

import com.example.answersboxapi.enums.UserEntityRole;
import com.example.answersboxapi.model.UserDetailsImpl;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


@UtilityClass
public class SecurityUtils {

    public static UserDetailsImpl getCurrentUser(){
        final Object auth = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (auth instanceof UserDetails){
            return (UserDetailsImpl) auth;
        }
        return null;
    }

    public static boolean isAdmin(){
        final UserDetailsImpl userDetails = getCurrentUser();
        return (userDetails != null && userDetails.getRole().equals(UserEntityRole.ROLE_ADMIN));
    }
}
