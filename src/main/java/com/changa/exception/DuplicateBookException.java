package com.changa.exception;

public class DuplicateBookException extends RuntimeException {
    public DuplicateBookException(String isbn) {
        super("Book with ISBN '%s' already exists".formatted(isbn));
    }
}
