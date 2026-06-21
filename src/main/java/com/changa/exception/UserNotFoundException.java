package com.changa.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException byId(UUID id) {
        return new UserNotFoundException(
                "User with ID '%s' does not exist".formatted(id)
        );
    }
}
