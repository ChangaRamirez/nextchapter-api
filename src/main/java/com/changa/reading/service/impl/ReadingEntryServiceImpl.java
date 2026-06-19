package com.changa.reading.service.impl;

import com.changa.book.domain.entity.Book;
import com.changa.book.repository.BookRepository;
import com.changa.exception.BookNotFoundException;
import com.changa.exception.DuplicateReadingEntryException;
import com.changa.exception.InvalidReadingEntryException;
import com.changa.exception.ReadingEntryNotFoundException;
import com.changa.reading.domain.CreateReadingEntryRequest;
import com.changa.reading.domain.UpdateReadingEntryRequest;
import com.changa.reading.domain.entity.ReadingEntry;
import com.changa.reading.domain.entity.ReadingStatus;
import com.changa.reading.repository.ReadingEntryRepository;
import com.changa.reading.service.ReadingEntryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class ReadingEntryServiceImpl implements ReadingEntryService {

    private final ReadingEntryRepository readingEntryRepository;
    private final BookRepository bookRepository;

    public ReadingEntryServiceImpl(ReadingEntryRepository readingEntryRepository, BookRepository bookRepository) {
        this.readingEntryRepository = readingEntryRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public ReadingEntry createReadingEntry(CreateReadingEntryRequest request) {

        Book existingBook = bookRepository.findById(request.bookId()).orElseThrow(() -> BookNotFoundException.byId(request.bookId()));

        if (readingEntryRepository.existsByBook_Id(request.bookId())) {
            throw new DuplicateReadingEntryException(request.bookId());
        }

        validateReadingEntryFields(request.status(), request.rating(), request.startedAt(), request.finishedAt());

        Instant now = Instant.now();

        ReadingEntry newReadingEntry = new ReadingEntry(
                null,
                existingBook,
                request.status(),
                request.rating(),
                request.startedAt(),
                request.finishedAt(),
                now,
                now
        );

        return readingEntryRepository.save(newReadingEntry);
    }

    @Override
    public ReadingEntry getReadingEntryById(UUID readingEntryId) {

        return readingEntryRepository.findById(readingEntryId).orElseThrow(() ->
                ReadingEntryNotFoundException.byId(readingEntryId));
    }

    @Override
    public Page<ReadingEntry> listReadingEntries(Pageable pageable) {
        return readingEntryRepository.findAll(pageable);
    }

    @Override
    public Page<ReadingEntry> searchRecentReadingEntries(Integer days, Pageable pageable) {
        if (days <= 0) {
            throw new InvalidReadingEntryException("Days must be positive");
        } else if (days >= 3650) {
            throw new InvalidReadingEntryException("Days must be less than 3650");
        }

        Instant from = Instant.now().minus(days, ChronoUnit.DAYS);

        return readingEntryRepository.findByCreatedGreaterThanEqual(from, pageable);
    }

    @Override
    public ReadingEntry updateReadingEntry(UUID readingEntryId, UpdateReadingEntryRequest request) {

        ReadingEntry existingReadingEntry = readingEntryRepository.findById(readingEntryId).orElseThrow(() ->
                ReadingEntryNotFoundException.byId(readingEntryId));

        validateReadingEntryFields(request.status(), request.rating(), request.startedAt(), request.finishedAt());

        existingReadingEntry.setStatus(request.status());
        existingReadingEntry.setRating(request.rating());
        existingReadingEntry.setStartedAt(request.startedAt());
        existingReadingEntry.setFinishedAt(request.finishedAt());

        existingReadingEntry.setUpdated(Instant.now());

        return readingEntryRepository.save(existingReadingEntry);
    }

    @Override
    public void deleteReadingEntry(UUID readingEntryId) {
        ReadingEntry existingReadingEntry = readingEntryRepository.findById(readingEntryId).orElseThrow(() ->
                ReadingEntryNotFoundException.byId(readingEntryId));

        readingEntryRepository.delete(existingReadingEntry);
    }

    private void validateReadingEntryFields(ReadingStatus status, Integer rating, LocalDate startedAt, LocalDate finishedAt) {
        if (status == ReadingStatus.TO_READ && rating != null) {
            throw new InvalidReadingEntryException("A TO_READ entry cannot have a rating.");
        }
        if (startedAt != null
        && finishedAt != null
        && finishedAt.isBefore(startedAt)) {
            throw new InvalidReadingEntryException("Finished date cannot be before started date.");
        }
        if (status == ReadingStatus.FINISHED && finishedAt == null) {
            throw new InvalidReadingEntryException("A FINISHED entry must have a finished date.");
        }
    }
}
