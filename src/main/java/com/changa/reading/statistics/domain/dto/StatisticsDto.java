package com.changa.reading.statistics.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record StatisticsDto(

        @Schema(
                description = "User's total number of reading entries.",
                example = "13"
        )
        long totalEntries,

        @Schema(
                description = "User's total number of finished books.",
                example = "8"
        )
        long booksFinished,

        @Schema(
                description = "User's number of books currently being read.",
                example = "8"
        )
        long booksReading,

        @Schema(
                description = "User's number of books to read.",
                example = "8"
        )
        long booksToRead,

        @Schema(
                description = "User's average rating across their own reading entries.",
                example = "7.68"
        )
        Double averageRating
) {}
