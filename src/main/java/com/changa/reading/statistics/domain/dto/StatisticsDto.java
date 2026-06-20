package com.changa.reading.statistics.domain.dto;

public record StatisticsDto(
        long totalEntries,
        long booksFinished,
        long booksReading,
        long booksToRead,
        Double averageRating
) {}
