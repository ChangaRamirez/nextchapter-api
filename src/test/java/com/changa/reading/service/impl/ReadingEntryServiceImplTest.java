package com.changa.reading.service.impl;

import com.changa.auth.service.AuthenticatedUserService;
import com.changa.book.domain.entity.Book;
import com.changa.book.repository.BookRepository;
import com.changa.exception.BookNotFoundException;
import com.changa.exception.DuplicateReadingEntryException;
import com.changa.exception.InvalidReadingEntryException;
import com.changa.exception.ReadingEntryNotFoundException;
import com.changa.reading.domain.CreateReadingEntryRequest;
import com.changa.reading.domain.UpdateReadingEntryReviewRequest;
import com.changa.reading.domain.UpdateReadingEntryRequest;
import com.changa.reading.domain.entity.ReadingEntry;
import com.changa.reading.domain.entity.ReadingNote;
import com.changa.reading.domain.entity.ReadingStatus;
import com.changa.reading.repository.ReadingEntryRepository;
import com.changa.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.changa.support.TestDataFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingEntryServiceImplTest {

    @Mock
    ReadingEntryRepository readingEntryRepository;

    @Mock
    BookRepository bookRepository;

    @Mock
    AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    ReadingEntryServiceImpl readingEntryService;

    @Test
    void createReadingEntry_shouldThrowDuplicateReadingEntryException_whenUserAlreadyHasBook() {
        CreateReadingEntryRequest request = new CreateReadingEntryRequest(
                UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c"),
                ReadingStatus.TO_READ,
                null,
                null,
                null
        );

        User currentUser = createUser();

        Book existingBook = createBook();

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);

        when(bookRepository.findById(request.bookId())).thenReturn(Optional.of(existingBook));

        when(readingEntryRepository.existsByUser_IdAndBook_Id(currentUser.getId(), request.bookId()))
                .thenReturn(true);

        assertThatThrownBy(() ->
                readingEntryService.createReadingEntry(request))
                .isInstanceOf(DuplicateReadingEntryException.class);

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));
    }

    @Test
    void createReadingEntry_shouldThrowBookNotFoundException_whenBookDoesNotExist() {
        CreateReadingEntryRequest request = new CreateReadingEntryRequest(
                UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c"),
                ReadingStatus.TO_READ,
                null,
                null,
                null
        );

        User currentUser = createUser();

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(bookRepository.findById(request.bookId())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                readingEntryService.createReadingEntry(request))
                .isInstanceOf(BookNotFoundException.class);

        verify(readingEntryRepository, never()).existsByUser_IdAndBook_Id(any(), any());

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));

    }

    @Test
    void createReadingEntry_shouldThrowInvalidReadingEntryException_whenToReadHasRating() {
        CreateReadingEntryRequest request = new CreateReadingEntryRequest(
                UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c"),
                ReadingStatus.TO_READ,
                10,
                null,
                null
        );

        User currentUser = createUser();
        Book existingBook = createBook();

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(bookRepository.findById(request.bookId())).thenReturn(Optional.of(existingBook));
        when(readingEntryRepository.existsByUser_IdAndBook_Id(currentUser.getId(), request.bookId())).thenReturn(false);

        assertThatThrownBy(() -> readingEntryService.createReadingEntry(request))
                .isInstanceOf(InvalidReadingEntryException.class)
                .hasMessageContaining("TO_READ");

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));
    }

    @Test
    void createReadingEntry_shouldThrowInvalidReadingEntryException_whenFinishedHasNoFinishedAt() {
        CreateReadingEntryRequest request = new CreateReadingEntryRequest(
                UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c"),
                ReadingStatus.FINISHED,
                10,
                LocalDate.now(),
                null
        );

        User currentUser = createUser();
        Book existingBook = createBook();

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(bookRepository.findById(request.bookId())).thenReturn(Optional.of(existingBook));
        when(readingEntryRepository.existsByUser_IdAndBook_Id(currentUser.getId(), request.bookId())).thenReturn(false);

        assertThatThrownBy(() -> readingEntryService.createReadingEntry(request))
                .isInstanceOf(InvalidReadingEntryException.class)
                .hasMessageContaining("FINISH");

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));
    }

    @Test
    void createReadingEntry_shouldThrowInvalidReadingEntryException_whenFinishedDateIsBeforeStartedDate() {
        CreateReadingEntryRequest request = new CreateReadingEntryRequest(
                UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c"),
                ReadingStatus.FINISHED,
                10,
                LocalDate.now().plus(1, ChronoUnit.DAYS),
                LocalDate.now()
        );

        User currentUser = createUser();
        Book existingBook = createBook();

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(bookRepository.findById(request.bookId())).thenReturn(Optional.of(existingBook));
        when(readingEntryRepository.existsByUser_IdAndBook_Id(currentUser.getId(), request.bookId())).thenReturn(false);

        assertThatThrownBy(() -> readingEntryService.createReadingEntry(request))
                .isInstanceOf(InvalidReadingEntryException.class)
                .hasMessageContaining("before");

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));
    }

    @Test
    void createReadingEntry_shouldSaveReadingEntry_whenRequestIsValid() {
        CreateReadingEntryRequest request = new CreateReadingEntryRequest(
                UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c"),
                ReadingStatus.TO_READ,
                null,
                null,
                null
        );

        User currentUser = createUser();
        Book existingBook = createBook();

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(bookRepository.findById(request.bookId())).thenReturn(Optional.of(existingBook));
        when(readingEntryRepository.existsByUser_IdAndBook_Id(currentUser.getId(), request.bookId())).thenReturn(false);

        when(readingEntryRepository.save(any(ReadingEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ReadingEntry response = readingEntryService.createReadingEntry(request);

        assertThat(response.getUser()).isEqualTo(currentUser);
        assertThat(response.getBook().getId()).isEqualTo(request.bookId());
        assertThat(response.getStatus()).isEqualTo(request.status());
        assertThat(response.getRating()).isEqualTo(request.rating());
        assertThat(response.getStartedAt()).isEqualTo(request.startedAt());
        assertThat(response.getFinishedAt()).isEqualTo(request.finishedAt());
        assertThat(response.getCreated()).isNotNull();
        assertThat(response.getUpdated()).isNotNull();
        assertThat(response.getReview()).isNull();
        assertThat(response.getNotes()).isEmpty();

    }

    @Test
    void listReadingEntries_shouldReturnOnlyCurrentUserEntries() {

        User currentUser = createUser();
        Pageable pageable = PageRequest.of(0,10);
        Page<ReadingEntry> expectedPage = new PageImpl<>(List.of());

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.findByUser_Id(currentUser.getId(), pageable)).thenReturn(expectedPage);

        Page<ReadingEntry> response = readingEntryService.listReadingEntries(pageable);

        assertThat(response).isEqualTo(expectedPage);

        verify(readingEntryRepository).findByUser_Id(currentUser.getId(), pageable);
    }

    @Test
    void getReadingEntryById_shouldReturnEntry_whenEntryBelongsToCurrentUser() {

        User currentUser = createUser();
        Book existingBook = createBook();

        UUID readingEntryId = UUID.fromString("b82780fe-c934-40e1-887e-9a0afa0dce91");

        ReadingEntry existingReadingEntry = new ReadingEntry(
                readingEntryId,
                currentUser,
                existingBook,
                ReadingStatus.TO_READ,
                null,
                null,
                null,
                null,
                Instant.now(),
                Instant.now()
        );

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.findByIdAndUser_Id(readingEntryId, currentUser.getId())).thenReturn(Optional.of(existingReadingEntry));

        ReadingEntry response = readingEntryService.getReadingEntryById(readingEntryId);

        assertThat(response).isEqualTo(existingReadingEntry);

        verify(readingEntryRepository).findByIdAndUser_Id(readingEntryId, currentUser.getId());
    }

    @Test
    void getReadingEntryById_shouldThrowReadingEntryNotFoundException_whenEntryDoesNotBelongToCurrentUser() {

        User currentUser = createUser();

        UUID readingEntryId = UUID.fromString("b82780fe-c934-40e1-887e-9a0afa0dce91");

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.findByIdAndUser_Id(readingEntryId, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readingEntryService.getReadingEntryById(readingEntryId))
                .isInstanceOf(ReadingEntryNotFoundException.class)
                .hasMessageContaining(readingEntryId.toString());
    }

    @Test
    void updateReadingEntry_shouldUpdateEntry_whenEntryBelongsToCurrentUser() {

        User currentUser = createUser();

        Book existingBook = createBook();

        ReadingEntry existingReadingEntry = createReadingEntry(currentUser, existingBook);

        UpdateReadingEntryRequest request = new UpdateReadingEntryRequest(
                ReadingStatus.FINISHED,
                8,
                LocalDate.now().minus(1, ChronoUnit.DAYS),
                LocalDate.now()
        );

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.findByIdAndUser_Id(existingReadingEntry.getId(), currentUser.getId())).thenReturn(Optional.of(existingReadingEntry));
        when(readingEntryRepository.save(any(ReadingEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant previousUpdated = existingReadingEntry.getUpdated();

        ReadingEntry response = readingEntryService.updateReadingEntry(existingReadingEntry.getId(), request);

        assertThat(response.getStatus()).isEqualTo(request.status());
        assertThat(response.getRating()).isEqualTo(request.rating());
        assertThat(response.getStartedAt()).isEqualTo(request.startedAt());
        assertThat(response.getFinishedAt()).isEqualTo(request.finishedAt());
        assertThat(response.getUpdated()).isAfterOrEqualTo(previousUpdated);
        assertThat(response.getReview()).isNull();
        assertThat(response.getNotes()).isEmpty();

        verify(readingEntryRepository).save(existingReadingEntry);
    }

    @Test
    void updateReadingEntry_shouldThrowReadingEntryNotFoundException_whenEntryDoesNotBelongToCurrentUser() {

        User currentUser = createUser();

        UUID readingEntryId = UUID.fromString("b3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c");

        UpdateReadingEntryRequest request = new UpdateReadingEntryRequest(
                ReadingStatus.FINISHED,
                8,
                LocalDate.now().minus(1, ChronoUnit.DAYS),
                LocalDate.now()
        );

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.findByIdAndUser_Id(readingEntryId, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readingEntryService.updateReadingEntry(readingEntryId, request))
                .isInstanceOf(ReadingEntryNotFoundException.class)
                .hasMessageContaining(readingEntryId.toString());

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));

    }

    @Test
    void updateReadingEntryReview_shouldUpdateEntryReview_whenEntryBelongsToCurrentUser() {

        User currentUser = createUser();

        Book existingBook = createBook();

        ReadingEntry existingReadingEntry = createReadingEntry(currentUser, existingBook);

        UpdateReadingEntryReviewRequest request = new UpdateReadingEntryReviewRequest(
                "Great book, bad ending. Would recommend."
        );

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.findByIdAndUser_Id(existingReadingEntry.getId(), currentUser.getId())).thenReturn(Optional.of(existingReadingEntry));
        when(readingEntryRepository.save(any(ReadingEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant previousUpdated = existingReadingEntry.getUpdated();

        ReadingEntry response = readingEntryService.updateReadingEntryReview(existingReadingEntry.getId(), request);

        assertThat(response.getReview()).isEqualTo(request.review());
        assertThat(response.getUpdated()).isAfterOrEqualTo(previousUpdated);
        assertThat(response.getStatus()).isEqualTo(existingReadingEntry.getStatus());
        assertThat(response.getNotes()).isEmpty();

        verify(readingEntryRepository).save(existingReadingEntry);
    }

    @Test
    void updateReadingEntryReview_shouldThrowReadingEntryNotFoundException_whenEntryDoesNotBelongToCurrentUser() {

        User currentUser = createUser();

        UUID readingEntryId = UUID.fromString("b3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c");

        UpdateReadingEntryReviewRequest request = new UpdateReadingEntryReviewRequest(
                "Great book, bad ending. Would recommend."
        );

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.findByIdAndUser_Id(readingEntryId, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readingEntryService.updateReadingEntryReview(readingEntryId, request))
                .isInstanceOf(ReadingEntryNotFoundException.class)
                .hasMessageContaining(readingEntryId.toString());

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));

    }

    @Test
    void updateReadingEntryReview_shouldThrowInvalidReadingEntryException_whenReviewIsAddedToToReadEntry() {

        User currentUser = createUser();

        Book existingBook = createBook();

        ReadingEntry existingReadingEntry = new ReadingEntry(
                UUID.fromString("b82780fe-c934-40e1-887e-9a0afa0dce91"),
                currentUser,
                existingBook,
                ReadingStatus.TO_READ,
                null,
                null,
                null,
                null,
                Instant.now(),
                Instant.now()
        );

        UpdateReadingEntryReviewRequest request = new UpdateReadingEntryReviewRequest(
                "Great book, bad ending. Would recommend."
        );

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.findByIdAndUser_Id(existingReadingEntry.getId(), currentUser.getId())).thenReturn(Optional.of(existingReadingEntry));

        assertThatThrownBy(() -> readingEntryService.updateReadingEntryReview(existingReadingEntry.getId(), request))
                .isInstanceOf(InvalidReadingEntryException.class)
                .hasMessageContaining("FINISHED or ABANDONED");

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));
    }

    @Test
    void updateReadingEntryReview_shouldThrowInvalidReadingEntryException_whenReviewRequestMessageIsEmpty() {

        User currentUser = createUser();

        Book existingBook = createBook();

        ReadingEntry existingReadingEntry = createReadingEntry(currentUser, existingBook);

        UpdateReadingEntryReviewRequest request = new UpdateReadingEntryReviewRequest("");

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.findByIdAndUser_Id(existingReadingEntry.getId(), currentUser.getId())).thenReturn(Optional.of(existingReadingEntry));

        assertThatThrownBy(() -> readingEntryService.updateReadingEntryReview(existingReadingEntry.getId(), request))
                .isInstanceOf(InvalidReadingEntryException.class)
                .hasMessageContaining("Review must not be blank.");

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));
    }

//    Reading notes:
//      - allow notes for every ReadingStatus
//      - verify TO_READ entries can create notes

    @Test
    void updateReadingEntry_shouldClearReview_whenStatusChangesToToRead() {

        User currentUser = createUser();

        Book existingBook = createBook();

        ReadingEntry existingReadingEntry = new ReadingEntry(
                UUID.fromString("b82780fe-c934-40e1-887e-9a0afa0dce91"),
                currentUser,
                existingBook,
                ReadingStatus.FINISHED,
                6,
                "Great book, bad ending. Would recommend.",
                LocalDate.parse("2025-01-15"),
                LocalDate.parse("2025-02-01"),
                Instant.now(),
                Instant.now()
        );

        UpdateReadingEntryRequest request = new UpdateReadingEntryRequest(
                ReadingStatus.TO_READ,
                null,
                null,
                null
        );

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.findByIdAndUser_Id(existingReadingEntry.getId(), currentUser.getId())).thenReturn(Optional.of(existingReadingEntry));
        when(readingEntryRepository.save(any(ReadingEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant previousUpdated = existingReadingEntry.getUpdated();

        ReadingEntry response = readingEntryService.updateReadingEntry(existingReadingEntry.getId(), request);

        assertThat(response.getStatus()).isEqualTo(request.status());
        assertThat(response.getRating()).isEqualTo(request.rating());
        assertThat(response.getStartedAt()).isEqualTo(request.startedAt());
        assertThat(response.getFinishedAt()).isEqualTo(request.finishedAt());
        assertThat(response.getUpdated()).isAfterOrEqualTo(previousUpdated);
        assertThat(response.getReview()).isNull();

        verify(readingEntryRepository).save(existingReadingEntry);
    }

    @Test
    void addNote_shouldSynchronizeBothSidesOfRelationship() {
        ReadingEntry readingEntry = createReadingEntry(
                createUser(),
                createBook()
        );

        ReadingNote note = createReadingNote();

        readingEntry.addNote(note);

        assertThat(readingEntry.getNotes())
                .containsExactly(note);

        assertThat(note.getReadingEntry())
                .isEqualTo(readingEntry);
    }

    @Test
    void removeNote_shouldSynchronizeBothSidesOfRelationship() {
        ReadingEntry readingEntry = createReadingEntry(
                createUser(),
                createBook()
        );

        ReadingNote note = createReadingNote();
        readingEntry.addNote(note);

        readingEntry.removeNote(note);

        assertThat(readingEntry.getNotes()).isEmpty();
        assertThat(note.getReadingEntry()).isNull();
    }

    @Test
    void deleteReadingEntry_shouldDeleteEntry_whenEntryBelongsToCurrentUser() {

        User currentUser = createUser();

        UUID readingEntryId = UUID.fromString("b82780fe-c934-40e1-887e-9a0afa0dce91");

        Book book = createBook();

        ReadingEntry existingEntry = createReadingEntry(currentUser, book);

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.findByIdAndUser_Id(readingEntryId, currentUser.getId())).thenReturn(Optional.of(existingEntry));

        readingEntryService.deleteReadingEntry(readingEntryId);

        verify(readingEntryRepository).delete(existingEntry);

    }

    @Test
    void deleteReadingEntry_shouldThrowReadingEntryNotFoundException_whenEntryDoesNotBelongToCurrentUser() {

        User currentUser = createUser();

        UUID readingEntryId = UUID.fromString("b82780fe-c934-40e1-887e-9a0afa0dce91");

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.findByIdAndUser_Id(readingEntryId, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> readingEntryService.deleteReadingEntry(readingEntryId))
                .isInstanceOf(ReadingEntryNotFoundException.class)
                .hasMessageContaining(readingEntryId.toString());

        verify(readingEntryRepository, never()).delete(any());
    }

    @Test
    void searchRecentReadingEntries_shouldReturnCurrentUserReadingEntries_whenDaysIsValid() {

        Pageable pageable = PageRequest.of(0,10);
        Page<ReadingEntry> expectedPage = new PageImpl<>(List.of());

        User currentUser = createUser();

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);

        when(readingEntryRepository.findByCreatedGreaterThanEqualAndUser_Id(
                any(Instant.class),
                eq(currentUser.getId()),
                eq(pageable)
        )).thenReturn(expectedPage);

        Page<ReadingEntry> response = readingEntryService.searchRecentReadingEntries(7, pageable);

        assertThat(response).isEqualTo(expectedPage);

        verify(readingEntryRepository).findByCreatedGreaterThanEqualAndUser_Id(
                any(Instant.class),
                eq(currentUser.getId()),
                eq(pageable)
        );

    }

    @Test
    void searchRecentReadingEntries_shouldThrowInvalidReadingEntryException_whenDaysIsNotPositive() {

        Pageable pageable = PageRequest.of(0,10);

        assertThatThrownBy(() -> readingEntryService.searchRecentReadingEntries(0, pageable))
                .isInstanceOf(InvalidReadingEntryException.class)
                .hasMessageContaining("must be positive");

        verifyNoInteractions(authenticatedUserService, readingEntryRepository);
    }

    @Test
    void searchRecentReadingEntries_shouldThrowInvalidReadingEntryException_whenDaysIsGreaterThanLimit() {

        Pageable pageable = PageRequest.of(0,10);

        assertThatThrownBy(() -> readingEntryService.searchRecentReadingEntries(3650, pageable))
                .isInstanceOf(InvalidReadingEntryException.class)
                .hasMessageContaining("must be less than");

        verifyNoInteractions(authenticatedUserService, readingEntryRepository);
    }

}