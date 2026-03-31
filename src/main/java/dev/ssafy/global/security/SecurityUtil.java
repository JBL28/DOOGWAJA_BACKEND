package dev.ssafy.global.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtil {

    private SecurityUtil() {
    }

    public static Optional<Long> getCurrentUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null 
                || authentication.getName().equals("anonymousUser")) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(authentication.getName()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
