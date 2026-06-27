package com.changa.reading.service;

import com.changa.reading.domain.CreateReadingEntryRequest;
import com.changa.reading.domain.UpdateReadingEntryNotesRequest;
import com.changa.reading.domain.UpdateReadingEntryRequest;
import com.changa.reading.domain.entity.ReadingEntry;
import com.changa.reading.domain.entity.ReadingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReadingEntryService {

    ReadingEntry createReadingEntry(CreateReadingEntryRequest request);

    ReadingEntry getReadingEntryById(UUID readingEntryId);

    Page<ReadingEntry> listReadingEntries(Pageable pageable);

    Page<ReadingEntry> searchRecentReadingEntries(Integer days, Pageable pageable);

    ReadingEntry updateReadingEntry(UUID readingEntryId, UpdateReadingEntryRequest request);

    ReadingEntry updateReadingEntryNotes(UUID readingEntryId, UpdateReadingEntryNotesRequest request);

    void deleteReadingEntry(UUID readingEntryId);
}
