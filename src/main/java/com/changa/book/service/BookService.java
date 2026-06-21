package com.changa.book.service;

import com.changa.book.domain.CreateBookRequest;
import com.changa.book.domain.UpdateBookRequest;
import com.changa.book.domain.entity.Book;
import com.changa.book.domain.entity.BookGenre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BookService {
    Book createBook(CreateBookRequest request);

    Page<Book> listBooks(Pageable pageable);

    Book getBookById(UUID bookId);

    Book getBookByIsbn(String bookIsbn);

    Page<Book> searchBooksByTitle(String bookTitle, Pageable pageable);

    Page<Book> searchBookByAuthor(String author, Pageable pageable);

    Page<Book> searchBooksByGenre(BookGenre genre, Pageable pageable);

    Page<Book> searchBooksByYearRange(Integer from, Integer to, Pageable pageable);

    Page<Book> searchBooks(String title, String author, BookGenre genre, Integer publicationYear, Pageable pageable);

    List<String> listAuthors();

    Book updateBook(UUID bookId, UpdateBookRequest request);

    void deleteBook(UUID bookId);
}
