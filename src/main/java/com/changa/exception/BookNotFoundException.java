package com.changa.exception;

import java.util.UUID;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(String message) {
        super(message);
    }

    public static BookNotFoundException byId(UUID id) {
        return new BookNotFoundException(
                "Book with ID '%s' does not exist".formatted(id)
        );
    }

    public static BookNotFoundException byIsbn(String isbn) {
        return new BookNotFoundException(
                "Book with ISBN '%s' does not exist".formatted(isbn)
        );
    }
}
