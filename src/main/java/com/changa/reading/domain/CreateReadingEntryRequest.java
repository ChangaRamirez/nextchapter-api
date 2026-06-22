package com.changa.reading.domain;

import com.changa.reading.domain.entity.ReadingStatus;
import com.changa.user.domain.entity.User;

import java.time.LocalDate;
import java.util.UUID;

public record CreateReadingEntryRequest(
        UUID bookId,
        ReadingStatus status,
        Integer rating,
        LocalDate startedAt,
        LocalDate finishedAt
) {}
