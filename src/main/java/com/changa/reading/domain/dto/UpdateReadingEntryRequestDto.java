package com.changa.reading.domain.dto;

import com.changa.reading.domain.entity.ReadingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateReadingEntryRequestDto(

        @Schema(
                description = "User's reading status of the book."
        )
        @NotNull (message = ERROR_MESSAGE_STATUS_REQUIRED)
        ReadingStatus status,

        @Schema(
                description = "User's rating of the book.",
                example = "8"
        )
        @Min(value = 0, message = ERROR_MESSAGE_RATING_RANGE)
        @Max(value = 10, message = ERROR_MESSAGE_RATING_RANGE)
        @Nullable
        Integer rating,

        @Schema(
                description = "User's reading start date.",
                example = "2025-05-17"
        )
        @PastOrPresent(message = ERROR_MESSAGE_STARTED_AT)
        @Nullable
        LocalDate startedAt,

        @Schema(
                description = "User's reading finish date.",
                example = "2026-07-08"
        )
        @PastOrPresent(message = ERROR_MESSAGE_FINISHED_AT)
        @Nullable
        LocalDate finishedAt

) {
    private static final String ERROR_MESSAGE_STATUS_REQUIRED =
            "Reading status must be provided";
    private static final String ERROR_MESSAGE_RATING_RANGE =
            "Rating must be between 0 and 10";
    private static final String ERROR_MESSAGE_STARTED_AT =
            "Started date must be in the past or present";
    private static final String ERROR_MESSAGE_FINISHED_AT =
            "Finished date must be in the past or present";
}
