package com.changa.book.domain.dto;

import com.changa.book.domain.entity.BookGenre;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record UpdateBookRequestDto(
        @Schema(
                description = "Book title.",
                example = "The Hobbit"
        )
        @NotBlank(message = ERROR_MESSAGE_TITLE)
        @Length(max = 255, message = ERROR_MESSAGE_TITLE_LENGTH)
        String title,

        @Schema(
                description = "Short description of the book.",
                example = "A fantasy novel about Bilbo Baggins' adventure."
        )
        @Length(max = 1000, message = ERROR_MESSAGE_DESCRIPTION_LENGTH)
        String description,

        @Schema(
                description = "Book author.",
                example = "J. R. R. Tolkien"
        )
        @NotBlank(message = ERROR_MESSAGE_AUTHOR)
        @Length(max = 255, message = ERROR_MESSAGE_AUTHOR)
        String author,

        @Schema(
                description = "Book genres."
        )
        @NotEmpty(message = ERROR_MESSAGE_GENRES)
        Set<BookGenre> genres,

        @Schema(
                description = "Publication year.",
                example = "1937"
        )
        @NotNull(message = ERROR_MESSAGE_PUBLICATION_YEAR_MISSING)
        @Min(value = 0, message = ERROR_MESSAGE_PUBLICATION_YEAR_INVALID)
        @Max(value = 2026, message = ERROR_MESSAGE_PUBLICATION_YEAR_UNREALISTIC)
        Integer publicationYear
) {
    private static final String ERROR_MESSAGE_TITLE =
            "Title must be provided";
    private static final String ERROR_MESSAGE_TITLE_LENGTH =
            "Title must be between 1 and 255 characters";
    private static final String ERROR_MESSAGE_DESCRIPTION_LENGTH =
            "Description must be less than 1000 characters";
    private static final String ERROR_MESSAGE_AUTHOR =
            "Author must be provided and less than 255 characters";
    private static final String ERROR_MESSAGE_GENRES =
            "At least 1 genre must be provided";
    private static final String ERROR_MESSAGE_PUBLICATION_YEAR_MISSING =
            "Publication year must be provided";
    private static final String ERROR_MESSAGE_PUBLICATION_YEAR_INVALID =
            "Publication year must be valid";
    private static final String ERROR_MESSAGE_PUBLICATION_YEAR_UNREALISTIC =
            "Publication year must be realistic";
}
