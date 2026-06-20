package com.changa.exception;

import java.util.UUID;

public class DuplicateReadingEntryException extends RuntimeException {
  public DuplicateReadingEntryException(UUID bookId) {
    super("Book with ID '%s' already exists".formatted(bookId));
  }
}
