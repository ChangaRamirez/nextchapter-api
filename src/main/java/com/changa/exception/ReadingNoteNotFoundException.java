package com.changa.exception;

import java.util.UUID;

public class ReadingNoteNotFoundException extends RuntimeException {
    public ReadingNoteNotFoundException(String message) {
        super(message);
    }

    public static ReadingNoteNotFoundException byId(UUID id) {
        return new ReadingNoteNotFoundException(
                "Reading Note with ID '%s' does not exist".formatted(id)
        );
    }
}
