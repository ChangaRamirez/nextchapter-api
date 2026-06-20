package com.changa.reading.domain.dto;

import com.changa.book.domain.entity.Book;
import com.changa.reading.domain.entity.ReadingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReadingEntryDto(
        UUID id,
        Book book,
        ReadingStatus status,
        Integer rating,
        LocalDate startedAt,
        LocalDate finishedAt
) {
}
