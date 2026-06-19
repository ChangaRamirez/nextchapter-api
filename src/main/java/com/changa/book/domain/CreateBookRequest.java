package com.changa.book.domain;

import com.changa.book.domain.entity.BookGenre;

import java.util.Set;

public record CreateBookRequest(
        String title,
        String description,
        String isbn,
        String author,
        Set<BookGenre> genres,
        Integer publicationYear
) {
}
