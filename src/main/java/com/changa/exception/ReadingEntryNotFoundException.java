package com.changa.exception;

import java.util.UUID;

public class ReadingEntryNotFoundException extends RuntimeException {
    public ReadingEntryNotFoundException(String message) {
        super(message);
    }

    public static ReadingEntryNotFoundException byId(UUID id) {
        return new ReadingEntryNotFoundException(
                "Reading Entry with ID '%s' does not exist".formatted(id)
        );
    }
}
