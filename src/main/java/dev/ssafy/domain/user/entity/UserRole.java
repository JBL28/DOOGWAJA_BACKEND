package dev.ssafy.domain.user.entity;

import lombok.Getter;

@Getter
public enum UserRole {
    USER("USER"),
    ADMIN("ADMIN");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }
}
