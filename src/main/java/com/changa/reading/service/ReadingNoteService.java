package com.changa.reading.service;

import com.changa.reading.domain.CreateReadingNoteRequest;
import com.changa.reading.domain.UpdateReadingNoteRequest;
import com.changa.reading.domain.entity.ReadingNote;

import java.util.UUID;

public interface ReadingNoteService {

    ReadingNote createReadingNote(UUID readingEntryId, CreateReadingNoteRequest request);

    ReadingNote updateReadingNote(UUID readingEntryId, UUID readingNoteId, UpdateReadingNoteRequest request);

    void deleteReadingNote(UUID readingEntryId, UUID readingNoteId);
}
