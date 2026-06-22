package com.changa.reading.domain.dto;

import com.changa.book.domain.entity.Book;
import com.changa.reading.domain.entity.ReadingStatus;

import java.time.LocalDate;
import java.util.UUID;

public record ReadingEntryDto(
        UUID id,
        UUID userId,
        UUID bookId,
        String bookTitle,
        ReadingStatus status,
        Integer rating,
        LocalDate startedAt,
        LocalDate finishedAt
) {
}
