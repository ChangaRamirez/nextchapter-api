package com.changa.book.domain.dto;

import com.changa.book.domain.entity.BookGenre;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

public record BookDto(

        @Schema(
                description = "Unique identifier or the book.",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        UUID id,

        @Schema(
                description = "Book title.",
                example = "The Hobbit"
        )
        String title,

        @Schema(
                description = "Short description of the book.",
                example = "A fantasy novel about Bilbo Baggins' adventure."
        )
        String description,

        @Schema(
                description = "International Standard Book Number.",
                example = "9780547928227"
        )
        String isbn,

        @Schema(
                description = "Book author.",
                example = "J. R. R. Tolkien"
        )
        String author,

        @Schema(
                description = "Book genres."
        )
        Set<BookGenre> genres,

        @Schema(
                description = "Publication year.",
                example = "1937"
        )
        Integer publicationYear
) {}
