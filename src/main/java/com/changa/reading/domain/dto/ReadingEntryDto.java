package com.changa.reading.domain.dto;

import com.changa.book.domain.entity.Book;
import com.changa.reading.domain.entity.ReadingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReadingEntryDto(
        @Schema(
                description = "Unique identifier of the reading entry.",
                example = "3d9616f5-b0af-4d0a-beab-dee7d7e2960e"
        )
        UUID id,

        @Schema(
                description = "Unique identifier of the user.",
                example = "cb82711d-d9be-4d13-8730-0d6cd6f82cc9"
        )
        UUID userId,

        @Schema(
                description = "Unique identifier of the book.",
                example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        )
        UUID bookId,

        @Schema(
                description = "Book title.",
                example = "The Hobbit"
        )
        String bookTitle,

        @Schema(
                description = "User's reading status of the book.",
                example = "FINISHED"
        )
        ReadingStatus status,

        @Schema(
                description = "User's rating of the book.",
                example = "8"
        )
        Integer rating,

        @Schema(
                description = "User's review of the book.",
                example = "Great book, would recommend. Weak ending though."
        )
        String review,

        @Schema(
                description = "User's private reading notes, ordered chronologically."
        )
        List<ReadingNoteDto> notes,

        @Schema(
                description = "User's reading start date.",
                example = "2025-05-17"
        )
        LocalDate startedAt,

        @Schema(
                description = "User's reading finish date.",
                example = "2026-07-08"
        )
        LocalDate finishedAt
) {
}
