package com.changa.reading.domain;

public record UpdateReadingEntryNotesRequest(
        String review,
        String notes
) {}
