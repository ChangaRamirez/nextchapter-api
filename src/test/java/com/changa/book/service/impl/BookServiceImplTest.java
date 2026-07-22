package com.changa.book.service.impl;

import com.changa.book.domain.CreateBookRequest;
import com.changa.book.domain.UpdateBookRequest;
import com.changa.book.domain.entity.Book;
import com.changa.book.domain.entity.BookGenre;
import com.changa.book.domain.entity.BookProvider;
import com.changa.book.repository.BookRepository;
import com.changa.exception.BookNotFoundException;
import com.changa.exception.DuplicateBookException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.changa.support.TestDataFactory.createBook;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void createBook_shouldThrowDuplicateBookException_whenIsbnAlreadyExists() {

        CreateBookRequest request = new CreateBookRequest(
                "Dune",
                "A sci-fi classic",
                "978-0441172719",
                "Frank Herbert",
                Set.of(
                        BookGenre.SCIENCE_FICTION,
                        BookGenre.ADVENTURE
                ),
                1965
        );

        when(bookRepository.existsByIsbn(request.isbn())).thenReturn(true);

        assertThatThrownBy(() -> bookService.createBook(request))
                .isInstanceOf(DuplicateBookException.class)
                .hasMessageContaining("already exists");

        verify(bookRepository, never()).save(any(Book.class));
        verify(bookRepository).existsByIsbn(request.isbn());
    }

    @Test
    void createBook_shouldSaveBook_whenIsbnDoesNotExist() {

        CreateBookRequest request = new CreateBookRequest(
                "Dune",
                "A sci-fi classic",
                "978-0441172719",
                "Frank Herbert",
                Set.of(
                        BookGenre.SCIENCE_FICTION,
                        BookGenre.ADVENTURE
                ),
                1965
        );

        when(bookRepository.existsByIsbn(request.isbn())).thenReturn(false);

        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Book createdBook = bookService.createBook(request);

        ArgumentCaptor<Book> bookCaptor =
                ArgumentCaptor.forClass(Book.class);

        verify(bookRepository).save(bookCaptor.capture());

        Book savedBook = bookCaptor.getValue();

        assertThat(savedBook.getTitle()).isEqualTo(request.title());
        assertThat(savedBook.getDescription()).isEqualTo(request.description());
        assertThat(savedBook.getIsbn()).isEqualTo(request.isbn());
        assertThat(savedBook.getAuthor()).isEqualTo(request.author());
        assertThat(savedBook.getGenres()).isEqualTo(request.genres());
        assertThat(savedBook.getPublicationYear()).isEqualTo(request.publicationYear());

        assertThat(savedBook.getCreated()).isNotNull();
        assertThat(savedBook.getUpdated()).isNotNull();
        assertThat(savedBook.getCreated()).isEqualTo(savedBook.getUpdated());

        assertThat(savedBook.getProvider()).isEqualTo(BookProvider.MANUAL);
        assertThat(savedBook.getExternalId()).isNull();
        assertThat(savedBook.getCoverUrl()).isNull();
        assertThat(savedBook.getMetadataFetchedAt()).isNull();

        assertThat(createdBook).isSameAs(savedBook);

        verify(bookRepository).existsByIsbn(request.isbn());

    }

    @Test
    void getBookById_shouldReturnBook_whenBookExists() {
        Book existingBook = createBook();
        when(bookRepository.findById(existingBook.getId())).thenReturn(Optional.of(existingBook));

        Book foundBook = bookService.getBookById(existingBook.getId());

        assertThat(foundBook).isEqualTo(existingBook);

        verify(bookRepository).findById(existingBook.getId());
    }

    @Test
    void getBookById_shouldThrowBookNotFoundException_whenBookDoesNotExist() {

        UUID bookId = UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c");

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(bookId))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("with ID");

        verify(bookRepository).findById(bookId);
    }

    @Test
    void updateBook_shouldUpdateBook_whenBookExists() {

        Book existingBook = createBook();
        UpdateBookRequest request= new UpdateBookRequest(
                "Dune Messiah",
                "A continuation to the saga",
                "Frank Herbert",
                Set.of(
                        BookGenre.SCIENCE_FICTION,
                        BookGenre.FICTION
                ),
                1990
        );

        when(bookRepository.findById(existingBook.getId())).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book updatedBook = bookService.updateBook(existingBook.getId(), request);

        assertThat(updatedBook.getTitle()).isEqualTo(request.title());
        assertThat(updatedBook.getDescription()).isEqualTo(request.description());
        assertThat(updatedBook.getAuthor()).isEqualTo(request.author());
        assertThat(updatedBook.getGenres()).isEqualTo(request.genres());
        assertThat(updatedBook.getPublicationYear()).isEqualTo(request.publicationYear());
        assertThat(updatedBook.getUpdated()).isNotNull();
        assertThat(updatedBook.getProvider()).isEqualTo(BookProvider.MANUAL);
        assertThat(updatedBook.getExternalId()).isNull();
        assertThat(updatedBook.getCoverUrl()).isNull();
        assertThat(updatedBook.getMetadataFetchedAt()).isNull();

        verify(bookRepository).save(existingBook);
    }

    @Test
    void updateBook_shouldThrowBookNotFoundException_whenBookDoesNotExist() {

        UUID bookId = UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c");

        UpdateBookRequest request= new UpdateBookRequest(
                "Dune Messiah",
                "A continuation to the saga",
                "Frank Herbert",
                Set.of(
                        BookGenre.SCIENCE_FICTION,
                        BookGenre.FICTION
                ),
                1990
        );

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(bookId, request))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("with ID");

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_shouldDeleteBook_whenBookExists() {

        Book existingBook = createBook();
        UUID bookId = existingBook.getId();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));

        bookService.deleteBook(bookId);

        verify(bookRepository).deleteById(bookId);
    }

    @Test
    void deleteBook_shouldThrowBookNotFoundException_whenBookDoesNotExist() {
        UUID bookId = UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c");

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.deleteBook(bookId))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("with ID");

        verify(bookRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void searchBooks_shouldCallRepositoryWithSpecificationAndPageable() {

        Pageable pageable = PageRequest.of(0,10);
        Page<Book> expectedPage = new PageImpl<>(List.of());

        when(bookRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        Page<Book> page = bookService.searchBooks("Dune", "Herbert", BookGenre.SCIENCE_FICTION, 1986, BookProvider.MANUAL, pageable);

        assertThat(page).isEqualTo(expectedPage);

        verify(bookRepository).findAll(any(Specification.class), eq(pageable));
    }

}