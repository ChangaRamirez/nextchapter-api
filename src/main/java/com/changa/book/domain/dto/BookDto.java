package com.changa.book.domain.dto;

import com.changa.book.domain.entity.BookGenre;

import java.util.Set;
import java.util.UUID;

public record BookDto(
        UUID id,
        String title,
        String description,
        String isbn,
        String author,
        Set<BookGenre> genres,
        Integer publicationYear
) {
}
