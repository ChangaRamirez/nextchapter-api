package com.changa.book.domain.dto;

import com.changa.book.domain.entity.BookGenre;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

public record UpdateBookRequestDto(
        @NotBlank(message = ERROR_MESSAGE_TITLE_LENGTH)
        @Length(max = 255, message = ERROR_MESSAGE_TITLE_LENGTH)
        String title,

        @Length(max = 1000, message = ERROR_MESSAGE_DESCRIPTION_LENGTH)
        String description,

        @NotBlank(message = ERROR_MESSAGE_AUTHOR)
        @Length(max = 255, message = ERROR_MESSAGE_AUTHOR)
        String author,

        @NotEmpty(message = ERROR_MESSAGE_GENRES)
        Set<BookGenre> genres,

        @NotNull(message = ERROR_MESSAGE_PUBLICATION_YEAR_MISSING)
        @Min(value = 0, message = ERROR_MESSAGE_PUBLICATION_YEAR_INVALID)
        @Max(value = 2026, message = ERROR_MESSAGE_PUBLICATION_YEAR_UNREALISTIC)
        Integer publicationYear
) {
    private static final String ERROR_MESSAGE_TITLE_LENGTH =
            "Title must be between 1 and 255 characters";
    private static final String ERROR_MESSAGE_DESCRIPTION_LENGTH =
            "Description must be less than 1000 characters";
    private static final String ERROR_MESSAGE_AUTHOR =
            "Author must be provided and less than 255 characters";
    private static final String ERROR_MESSAGE_GENRES =
            "At least 1 genre must be provided";
    private static final String ERROR_MESSAGE_PUBLICATION_YEAR_MISSING =
            "Publication must be provided";
    private static final String ERROR_MESSAGE_PUBLICATION_YEAR_INVALID =
            "Publication year must be valid";
    private static final String ERROR_MESSAGE_PUBLICATION_YEAR_UNREALISTIC =
            "Publication year must be realistic";
}
