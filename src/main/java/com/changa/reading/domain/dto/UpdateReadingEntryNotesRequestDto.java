package com.changa.reading.domain.dto;

import com.changa.reading.domain.entity.ReadingStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

public record UpdateReadingEntryNotesRequestDto(

        @Length(max = 2000, message = ERROR_MESSAGE_REVIEW_LENGTH)
        @Nullable
        String review,

        @Length(max = 5000, message = ERROR_MESSAGE_NOTES_LENGTH)
        @Nullable
        String notes

) {
    private static final String ERROR_MESSAGE_REVIEW_LENGTH =
            "Review must be at most 2000 characters";
    private static final String ERROR_MESSAGE_NOTES_LENGTH =
            "Notes must be at most 5000 characters";
}
