package com.changa.book.service.impl;

import com.changa.book.domain.CreateBookRequest;
import com.changa.book.domain.UpdateBookRequest;
import com.changa.book.domain.entity.Book;
import com.changa.book.domain.entity.BookGenre;
import com.changa.exception.BookNotFoundException;
import com.changa.exception.DuplicateBookException;
import com.changa.book.repository.BookRepository;
import com.changa.book.service.BookService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public Book createBook(CreateBookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new DuplicateBookException(request.isbn());
        }

        Instant now = Instant.now();

        Book newBook = new Book(
                null,
                request.title(),
                request.description(),
                request.isbn(),
                request.author(),
                request.genres(),
                request.publicationYear(),
                now,
                now
        );

        return bookRepository.save(newBook);
    }

    @Override
    public Page<Book> listBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Override
    public Book getBookById(UUID bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> BookNotFoundException.byId(bookId));
    }

    @Override
    public Book getBookByIsbn(String bookIsbn) {

        return bookRepository
                .findByIsbn(bookIsbn)
                .orElseThrow(() -> BookNotFoundException.byIsbn(bookIsbn));
    }

    @Override
    public Page<Book> searchBooksByTitle(String bookTitle, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(bookTitle, pageable);
    }


    @Override
    public Page<Book> searchBookByAuthor(String author, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCase(author, pageable);
    }

    @Override
    public Page<Book> searchBooksByGenre(BookGenre genre, Pageable pageable) {
        return bookRepository.findByGenresContaining(genre, pageable);
    }

    @Override
    public Page<Book> searchBooksByYearRange(Integer from, Integer to, Pageable pageable) {
        return bookRepository.findByPublicationYearBetween(from, to, pageable);
    }

    @Override
    public List<String> listAuthors() {
        return bookRepository.findDistinctAuthors();
    }

    @Override
    public Book updateBook(UUID bookId, UpdateBookRequest request) {
        Book existingBook = bookRepository.findById(bookId).orElseThrow(() -> BookNotFoundException.byId(bookId));

        existingBook.setTitle(request.title());
        existingBook.setDescription(request.description());
        existingBook.setAuthor(request.author());
        existingBook.setGenres(request.genres());
        existingBook.setPublicationYear(request.publicationYear());

        existingBook.setUpdated(Instant.now());

        return bookRepository.save(existingBook);
    }

    @Override
    public void deleteBook(UUID bookId) {
        Book existingBook = bookRepository.findById(bookId).orElseThrow(() -> BookNotFoundException.byId(bookId));

        bookRepository.deleteById(bookId);
    }
}
