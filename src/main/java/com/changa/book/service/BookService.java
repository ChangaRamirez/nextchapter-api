package com.changa.book.service;

import com.changa.book.domain.CreateBookRequest;
import com.changa.book.domain.UpdateBookRequest;
import com.changa.book.domain.entity.Book;
import com.changa.book.domain.entity.BookGenre;
import com.changa.book.domain.entity.BookProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BookService {
    Book createBook(CreateBookRequest request);

    Page<Book> listBooks(Pageable pageable);

    Book getBookById(UUID bookId);

    Book getBookByIsbn(String bookIsbn);

    Page<Book> searchBooks(String title, String author, BookGenre genre, Integer publicationYear, BookProvider provider, Pageable pageable);

    List<String> listAuthors();

    Book updateBook(UUID bookId, UpdateBookRequest request);

    void deleteBook(UUID bookId);
}
