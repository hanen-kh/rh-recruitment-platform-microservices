package com.example.recruitment_service.Application;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public record Caller(String subject, Set<String> roles) {

    private static final String PREFIX = "ROLE_";
    private static final String[] RECRUITMENT_ROLES = {"HR_MANAGER", "ADMIN"};

    public static Caller from(Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith(PREFIX))
                .map(authority -> authority.substring(PREFIX.length()))
                .collect(Collectors.toUnmodifiableSet());
        return new Caller(authentication.getName(), roles);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasAnyRole(String... candidates) {
        return Arrays.stream(candidates).anyMatch(roles::contains);
    }

    public boolean staff() {
        return hasAnyRole(RECRUITMENT_ROLES);
    }

    public boolean isSubject(String id) {
        return subject.equals(id);
    }
}