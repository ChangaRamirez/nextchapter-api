package com.changa.reading.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import org.hibernate.validator.constraints.Length;

public record UpdateReadingEntryReviewRequestDto(

        @Schema(
                description = "User's review of the book.",
                example = "Great book, would recommend. Weak ending though."
        )
        @Nullable
        @Length(max = 2000, message = ERROR_MESSAGE_REVIEW_LENGTH)
        String review

) {
    private static final String ERROR_MESSAGE_REVIEW_LENGTH =
            "Review must be at most 2000 characters";
    private static final String ERROR_MESSAGE_NOTES_LENGTH =
            "Notes must be at most 5000 characters";
}
