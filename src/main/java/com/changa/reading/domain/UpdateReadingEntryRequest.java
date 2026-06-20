package com.changa.reading.domain;

import com.changa.reading.domain.entity.ReadingStatus;

import java.time.LocalDate;

public record UpdateReadingEntryRequest(
        ReadingStatus status,
        Integer rating,
        LocalDate startedAt,
        LocalDate finishedAt
) {}
