package com.changa.reading.domain.dto;

import com.changa.reading.domain.entity.ReadingStatus;
import jakarta.annotation.Nullable;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record CreateReadingEntryRequestDto(

        @NotNull(message = ERROR_MESSAGE_BOOK_ID_REQUIRED)
        UUID bookId,

        @NotNull (message = ERROR_MESSAGE_STATUS_REQUIRED)
        ReadingStatus status,

        @Min(value = 0, message = ERROR_MESSAGE_RATING_RANGE)
        @Max(value = 10, message = ERROR_MESSAGE_RATING_RANGE)
        @Nullable
        Integer rating,

        @PastOrPresent(message = ERROR_MESSAGE_STARTED_AT)
        @Nullable
        LocalDate startedAt,

        @PastOrPresent(message = ERROR_MESSAGE_FINISHED_AT)
        @Nullable
        LocalDate finishedAt

) {
    private static final String ERROR_MESSAGE_BOOK_ID_REQUIRED =
            "Book ID must be provided";
    private static final String ERROR_MESSAGE_STATUS_REQUIRED =
            "Reading status must be provided";
    private static final String ERROR_MESSAGE_RATING_RANGE =
            "Rating must be between 0 and 10";
    private static final String ERROR_MESSAGE_STARTED_AT =
            "Started date must be in the past or present";
    private static final String ERROR_MESSAGE_FINISHED_AT =
            "Finished date must be in the past or present";
}
