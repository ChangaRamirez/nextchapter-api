package com.changa.reading.service.impl;

import com.changa.auth.service.AuthenticatedUserService;
import com.changa.exception.ReadingEntryNotFoundException;
import com.changa.exception.ReadingNoteNotFoundException;
import com.changa.reading.domain.CreateReadingNoteRequest;
import com.changa.reading.domain.UpdateReadingNoteRequest;
import com.changa.reading.domain.entity.ReadingEntry;
import com.changa.reading.domain.entity.ReadingNote;
import com.changa.reading.repository.ReadingEntryRepository;
import com.changa.reading.repository.ReadingNoteRepository;
import com.changa.reading.service.ReadingNoteService;
import com.changa.user.domain.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class ReadingNoteServiceImpl implements ReadingNoteService {

    private final ReadingNoteRepository readingNoteRepository;
    private final ReadingEntryRepository readingEntryRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public ReadingNoteServiceImpl(ReadingNoteRepository readingNoteRepository, ReadingEntryRepository readingEntryRepository, AuthenticatedUserService authenticatedUserService) {
        this.readingNoteRepository = readingNoteRepository;
        this.readingEntryRepository = readingEntryRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional
    @Override
    public ReadingNote createReadingNote(UUID readingEntryId, CreateReadingNoteRequest request) {
        User user = authenticatedUserService.getCurrentUser();

        ReadingEntry existingReadingEntry =  readingEntryRepository.findByIdAndUser_Id(readingEntryId, user.getId()).orElseThrow(() ->
                ReadingEntryNotFoundException.byId(readingEntryId));

        Instant now = Instant.now();

        ReadingNote newReadingNote = new ReadingNote(
                null,
                request.content(),
                now,
                now
        );

        existingReadingEntry.addNote(newReadingNote);
        existingReadingEntry.setUpdated(now);

        readingEntryRepository.flush();
        return newReadingNote;
    }

    @Transactional
    @Override
    public ReadingNote updateReadingNote(UUID readingEntryId, UUID readingNoteId, UpdateReadingNoteRequest request) {

        User user = authenticatedUserService.getCurrentUser();

        ReadingEntry existingReadingEntry = readingEntryRepository.findByIdAndUser_Id(readingEntryId, user.getId()).orElseThrow(() ->
                ReadingEntryNotFoundException.byId(readingEntryId));

        ReadingNote existingReadingNote = readingNoteRepository.findById(readingNoteId).orElseThrow(() ->
                ReadingNoteNotFoundException.byId(readingNoteId));

        if (!existingReadingNote
                .getReadingEntry()
                .equals(existingReadingEntry)) {

            throw ReadingNoteNotFoundException.byId(readingNoteId);
        }

        Instant now = Instant.now();

        existingReadingNote.setContent(request.content());
        existingReadingNote.setUpdated(now);
        existingReadingEntry.setUpdated(now);

        return existingReadingNote;
    }

    @Transactional
    @Override
    public void deleteReadingNote(UUID readingEntryId, UUID readingNoteId) {

        User user = authenticatedUserService.getCurrentUser();

        ReadingEntry existingReadingEntry = readingEntryRepository.findByIdAndUser_Id(readingEntryId, user.getId()).orElseThrow(() ->
                ReadingEntryNotFoundException.byId(readingEntryId));

        ReadingNote existingReadingNote = readingNoteRepository.findById(readingNoteId).orElseThrow(() ->
                ReadingNoteNotFoundException.byId(readingNoteId));

        if (!existingReadingNote
                .getReadingEntry()
                .equals(existingReadingEntry)) {

            throw ReadingNoteNotFoundException.byId(readingNoteId);
        }

        existingReadingEntry.removeNote(existingReadingNote);
        existingReadingEntry.setUpdated(Instant.now());
    }
}
