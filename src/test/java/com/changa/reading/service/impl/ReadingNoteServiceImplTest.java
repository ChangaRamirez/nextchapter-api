package com.changa.reading.service.impl;

import com.changa.auth.service.AuthenticatedUserService;
import com.changa.book.domain.entity.Book;
import com.changa.book.domain.entity.BookGenre;
import com.changa.exception.*;
import com.changa.reading.domain.*;
import com.changa.reading.domain.entity.ReadingEntry;
import com.changa.reading.domain.entity.ReadingNote;
import com.changa.reading.domain.entity.ReadingStatus;
import com.changa.reading.repository.ReadingEntryRepository;
import com.changa.reading.repository.ReadingNoteRepository;
import com.changa.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static com.changa.support.TestDataFactory.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReadingNoteServiceImplTest {

    @Mock
    private ReadingNoteRepository readingNoteRepository;

    @Mock
    private ReadingEntryRepository readingEntryRepository;

    @Mock
    private AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    private ReadingNoteServiceImpl readingNoteService;

    @Test
    void createReadingNote_shouldThrowReadingEntryNotFoundException_whenReadingEntryDoesNotBelongToCurrentUser() {
        UUID readingEntryId = UUID.fromString("b82780fe-c934-40e1-887e-9a0afa0dce91");

        CreateReadingNoteRequest request = new CreateReadingNoteRequest("Must finish this book before the summer ends.");

        User currentUser = createUser();

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);

        when(readingEntryRepository.findByIdAndUser_Id(readingEntryId, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                readingNoteService.createReadingNote(readingEntryId, request))
                .isInstanceOf(ReadingEntryNotFoundException.class)
                .hasMessageContaining(readingEntryId.toString());

        verify(readingEntryRepository, never()).flush();
        verifyNoInteractions(readingNoteRepository);
    }

    @Test
    void createReadingNote_shouldAddNoteToEntry_whenRequestIsValid() {
        User currentUser = createUser();
        Book existingBook = createBook();
        ReadingEntry existingReadingEntry =
                createReadingEntry(currentUser, existingBook);

        existingReadingEntry.setUpdated(Instant.parse("2020-01-01T00:00:00Z"));
        Instant previousEntryUpdated = existingReadingEntry.getUpdated();

        CreateReadingNoteRequest request =
                new CreateReadingNoteRequest(
                        "Must finish this book before the summer ends."
                );

        when(authenticatedUserService.getCurrentUser())
                .thenReturn(currentUser);

        when(readingEntryRepository.findByIdAndUser_Id(
                existingReadingEntry.getId(),
                currentUser.getId()
        )).thenReturn(Optional.of(existingReadingEntry));

        ReadingNote response =
                readingNoteService.createReadingNote(
                        existingReadingEntry.getId(),
                        request
                );

        assertThat(response.getContent()).isEqualTo(request.content());
        assertThat(response.getReadingEntry()).isSameAs(existingReadingEntry);
        assertThat(response.getCreated()).isNotNull();
        assertThat(response.getUpdated()).isNotNull();

        assertThat(existingReadingEntry.getNotes())
                .containsExactly(response);

        assertThat(existingReadingEntry.getUpdated())
                .isAfter(previousEntryUpdated);

        verify(readingEntryRepository).flush();
        verify(readingEntryRepository, never())
                .save(any(ReadingEntry.class));

        verifyNoInteractions(readingNoteRepository);
    }

    @Test
    void updateReadingNote_shouldThrowReadingEntryNotFoundException_whenReadingEntryDoesNotExist() {
        UUID readingEntryId = UUID.fromString("b82780fe-c934-40e1-887e-9a0afa0dce91");
        UUID readingNoteId = UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c");

        UpdateReadingNoteRequest request = new UpdateReadingNoteRequest("Must finish this book before the summer ends.");

        User currentUser = createUser();

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);

        when(readingEntryRepository.findByIdAndUser_Id(readingEntryId, currentUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                readingNoteService.updateReadingNote(readingEntryId, readingNoteId, request))
                .isInstanceOf(ReadingEntryNotFoundException.class)
                .hasMessageContaining(readingEntryId.toString());

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));
        verify(readingNoteRepository, never()).save(any(ReadingNote.class));
    }

    @Test
    void updateReadingNote_shouldThrowReadingNoteNotFoundException_whenReadingNoteDoesNotExist() {
        User currentUser = createUser();
        Book existingBook = createBook();
        ReadingEntry existingReadingEntry = createReadingEntry(currentUser, existingBook);
        UUID readingNoteId = UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c");

        UpdateReadingNoteRequest request = new UpdateReadingNoteRequest("Must finish this book before the summer ends.");


        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);

        when(readingEntryRepository.findByIdAndUser_Id(existingReadingEntry.getId(), currentUser.getId())).thenReturn(Optional.of(existingReadingEntry));
        when(readingNoteRepository.findById(readingNoteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                readingNoteService.updateReadingNote(existingReadingEntry.getId(), readingNoteId, request))
                .isInstanceOf(ReadingNoteNotFoundException.class)
                .hasMessageContaining(readingNoteId.toString());

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));
        verify(readingNoteRepository, never()).save(any(ReadingNote.class));
    }

    @Test
    void updateReadingNote_shouldThrowReadingNoteNotFoundException_whenNoteDoesNotBelongToReadingEntry() {
        // Creating Reading Entry A
        User currentUser = createUser();
        Book existingBookA = createBook();
        ReadingEntry existingReadingEntryA = createReadingEntry(currentUser, existingBookA);
        ReadingNote existingReadingNoteA = createReadingNote();
        existingReadingEntryA.addNote(existingReadingNoteA);

        // Creating Reading Entry B
        Book existingBookB = new Book(
                UUID.fromString("c8fea603-5f18-4b81-8f06-1d68dae16f64"),
                "The Hobbit",
                "A fantasy novel about Bilbo Baggins' adventure.",
                "978-0547928227",
                "J.R.R. Tolkien",
                Set.of(
                        BookGenre.FANTASY,
                        BookGenre.ADVENTURE
                ),
                1937,
                Instant.now(),
                Instant.now()
        );

        ReadingEntry existingReadingEntryB = new ReadingEntry(
                UUID.fromString("b9420862-fffe-4d90-b698-0fe96c42faf6"),
                currentUser,
                existingBookB,
                ReadingStatus.FINISHED,
                9,
                null,
                LocalDate.parse("2025-01-15"),
                LocalDate.parse("2025-02-01"),
                Instant.now(),
                Instant.now()
        );
        ReadingNote existingReadingNoteB = new ReadingNote(
                UUID.fromString("b9420862-fffe-4d90-b698-0fe96c42faf6"),
                "This is a note",
                Instant.now(),
                Instant.now()
        );

        existingReadingEntryB.addNote(existingReadingNoteB);

        Instant entryUpdated = existingReadingEntryA.getUpdated();
        Instant otherEntryUpdated = existingReadingEntryB.getUpdated();
        Instant noteUpdated = existingReadingNoteB.getUpdated();
        String content = existingReadingNoteB.getContent();

        UpdateReadingNoteRequest request = new UpdateReadingNoteRequest("Must finish this book before the winter ends.");

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);

        when(readingEntryRepository.findByIdAndUser_Id(existingReadingEntryA.getId(), currentUser.getId())).thenReturn(Optional.of(existingReadingEntryA));
        when(readingNoteRepository.findById(existingReadingNoteB.getId())).thenReturn(Optional.of(existingReadingNoteB));

        assertThatThrownBy(() ->
                readingNoteService.updateReadingNote(existingReadingEntryA.getId(), existingReadingNoteB.getId(), request))
                .isInstanceOf(ReadingNoteNotFoundException.class)
                .hasMessageContaining(existingReadingNoteB.getId().toString());

        assertThat(existingReadingEntryA.getUpdated()).isEqualTo(entryUpdated);
        assertThat(existingReadingEntryB.getUpdated()).isEqualTo(otherEntryUpdated);
        assertThat(existingReadingNoteB.getContent()).isEqualTo(content);
        assertThat(existingReadingNoteB.getUpdated()).isEqualTo(noteUpdated);
        assertThat(existingReadingNoteB.getReadingEntry()).isSameAs(existingReadingEntryB);
        assertThat(existingReadingEntryA.getNotes()).containsExactly(existingReadingNoteA);
        assertThat(existingReadingEntryB.getNotes()).containsExactly(existingReadingNoteB);

        verify(readingEntryRepository,never()).save(any(ReadingEntry.class));
        verify(readingNoteRepository, never()).save(any(ReadingNote.class));
    }

    @Test
    void updateReadingNote_shouldUpdateNote_whenNoteBelongsToReadingEntry() {
        User currentUser = createUser();
        Book existingBook = createBook();
        ReadingEntry existingReadingEntry = createReadingEntry(currentUser, existingBook);
        ReadingNote existingReadingNote = createReadingNote();
        existingReadingEntry.addNote(existingReadingNote);

        UpdateReadingNoteRequest request = new UpdateReadingNoteRequest("This is the updated note.");

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);

        when(readingEntryRepository.findByIdAndUser_Id(existingReadingEntry.getId(), currentUser.getId())).thenReturn(Optional.of(existingReadingEntry));
        when(readingNoteRepository.findById(existingReadingNote.getId())).thenReturn(Optional.of(existingReadingNote));

        existingReadingEntry.setUpdated(Instant.parse("2020-01-01T00:00:00Z"));
        Instant entryPastUpdated = existingReadingEntry.getUpdated();
        existingReadingNote.setUpdated(Instant.parse("2020-01-01T00:00:00Z"));
        Instant notePastUpdated = existingReadingNote.getUpdated();

        ReadingNote response = readingNoteService.updateReadingNote(existingReadingEntry.getId(), existingReadingNote.getId(), request);

        assertThat(response.getContent()).isEqualTo(request.content());
        assertThat(response.getUpdated()).isAfter(notePastUpdated);
        assertThat(response.getReadingEntry().getUpdated()).isAfter(entryPastUpdated);

        assertThat(response).isSameAs(existingReadingNote);
        assertThat(response.getReadingEntry()).isSameAs(existingReadingEntry);

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));
        verify(readingNoteRepository, never()).save(any(ReadingNote.class));
    }

    @Test
    void deleteReadingNote_shouldThrowReadingNoteNotFoundException_whenReadingNoteDoesNotExist() {
        User currentUser = createUser();
        Book existingBook = createBook();
        ReadingEntry existingReadingEntry = createReadingEntry(currentUser, existingBook);
        UUID readingNoteId = UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c");

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);

        when(readingEntryRepository.findByIdAndUser_Id(existingReadingEntry.getId(), currentUser.getId())).thenReturn(Optional.of(existingReadingEntry));
        when(readingNoteRepository.findById(readingNoteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                readingNoteService.deleteReadingNote(existingReadingEntry.getId(), readingNoteId))
                .isInstanceOf(ReadingNoteNotFoundException.class)
                .hasMessageContaining(readingNoteId.toString());

        assertThat(existingReadingEntry.getNotes()).isEmpty();

        verify(readingEntryRepository,never()).save(any(ReadingEntry.class));
        verify(readingNoteRepository, never()).save(any(ReadingNote.class));
    }

    @Test
    void deleteReadingNote_shouldThrowReadingNoteNotFoundException_whenNoteDoesNotBelongToReadingEntry() {
        // Creating Reading Entry A
        User currentUser = createUser();
        Book existingBookA = createBook();
        ReadingEntry existingReadingEntryA = createReadingEntry(currentUser, existingBookA);
        ReadingNote existingReadingNoteA = createReadingNote();
        existingReadingEntryA.addNote(existingReadingNoteA);

        // Creating Reading Entry B
        Book existingBookB = new Book(
                UUID.fromString("c8fea603-5f18-4b81-8f06-1d68dae16f64"),
                "The Hobbit",
                "A fantasy novel about Bilbo Baggins' adventure.",
                "978-0547928227",
                "J.R.R. Tolkien",
                Set.of(
                        BookGenre.FANTASY,
                        BookGenre.ADVENTURE
                ),
                1937,
                Instant.now(),
                Instant.now()
        );

        ReadingEntry existingReadingEntryB = new ReadingEntry(
                UUID.fromString("b9420862-fffe-4d90-b698-0fe96c42faf6"),
                currentUser,
                existingBookB,
                ReadingStatus.FINISHED,
                9,
                null,
                LocalDate.parse("2025-01-15"),
                LocalDate.parse("2025-02-01"),
                Instant.now(),
                Instant.now()
        );
        ReadingNote existingReadingNoteB = new ReadingNote(
                UUID.fromString("b9420862-fffe-4d90-b698-0fe96c42faf6"),
                "This is a note",
                Instant.now(),
                Instant.now()
        );

        existingReadingEntryB.addNote(existingReadingNoteB);

        Instant entryUpdated = existingReadingEntryA.getUpdated();
        Instant otherEntryUpdated = existingReadingEntryB.getUpdated();
        Instant noteUpdated = existingReadingNoteB.getUpdated();
        String content = existingReadingNoteB.getContent();

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);

        when(readingEntryRepository.findByIdAndUser_Id(existingReadingEntryA.getId(), currentUser.getId())).thenReturn(Optional.of(existingReadingEntryA));
        when(readingNoteRepository.findById(existingReadingNoteB.getId())).thenReturn(Optional.of(existingReadingNoteB));

        assertThatThrownBy(() ->
                readingNoteService.deleteReadingNote(existingReadingEntryA.getId(), existingReadingNoteB.getId()))
                .isInstanceOf(ReadingNoteNotFoundException.class)
                .hasMessageContaining(existingReadingNoteB.getId().toString());

        assertThat(existingReadingEntryA.getUpdated()).isEqualTo(entryUpdated);
        assertThat(existingReadingEntryB.getUpdated()).isEqualTo(otherEntryUpdated);
        assertThat(existingReadingNoteB.getContent()).isEqualTo(content);
        assertThat(existingReadingNoteB.getUpdated()).isEqualTo(noteUpdated);
        assertThat(existingReadingNoteB.getReadingEntry()).isSameAs(existingReadingEntryB);
        assertThat(existingReadingEntryA.getNotes()).containsExactly(existingReadingNoteA);
        assertThat(existingReadingEntryB.getNotes()).containsExactly(existingReadingNoteB);

        verify(readingEntryRepository,never()).save(any(ReadingEntry.class));
        verify(readingNoteRepository, never()).save(any(ReadingNote.class));
    }

    @Test
    void deleteReadingNote_shouldDeleteNote_whenNoteBelongsToReadingEntry() {
        User currentUser = createUser();
        Book existingBook = createBook();
        ReadingEntry existingReadingEntry = createReadingEntry(currentUser, existingBook);
        ReadingNote existingReadingNote = createReadingNote();
        existingReadingEntry.addNote(existingReadingNote);

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);

        when(readingEntryRepository.findByIdAndUser_Id(existingReadingEntry.getId(), currentUser.getId())).thenReturn(Optional.of(existingReadingEntry));
        when(readingNoteRepository.findById(existingReadingNote.getId())).thenReturn(Optional.of(existingReadingNote));

        existingReadingEntry.setUpdated(Instant.parse("2020-01-01T00:00:00Z"));
        Instant entryPastUpdated = existingReadingEntry.getUpdated();

        readingNoteService.deleteReadingNote(existingReadingEntry.getId(), existingReadingNote.getId());

        assertThat(existingReadingEntry.getNotes()).doesNotContain(existingReadingNote);
        assertThat(existingReadingNote.getReadingEntry()).isNull();
        assertThat(existingReadingEntry.getUpdated()).isAfter(entryPastUpdated);
        assertThat(existingReadingEntry.getNotes()).isEmpty();

        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));
        verify(readingNoteRepository, never()).save(any(ReadingNote.class));
    }

    @Test
    void deleteReadingNote_shouldRejectMissingOrUnownedEntry_beforeLookingUpNote() {
        User user = createUser();
        UUID entryId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        when(authenticatedUserService.getCurrentUser()).thenReturn(user);
        when(readingEntryRepository.findByIdAndUser_Id(entryId, user.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> readingNoteService.deleteReadingNote(entryId, noteId))
                .isInstanceOf(ReadingEntryNotFoundException.class)
                .hasMessageContaining(entryId.toString());

        verifyNoInteractions(readingNoteRepository);
        verify(readingEntryRepository, never()).save(any(ReadingEntry.class));
    }
}
