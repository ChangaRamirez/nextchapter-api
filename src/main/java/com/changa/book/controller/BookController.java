package com.changa.book.controller;

import com.changa.book.domain.CreateBookRequest;
import com.changa.book.domain.UpdateBookRequest;
import com.changa.book.domain.dto.BookDto;
import com.changa.book.domain.dto.CreateBookRequestDto;
import com.changa.book.domain.dto.UpdateBookRequestDto;
import com.changa.book.domain.entity.Book;
import com.changa.book.domain.entity.BookGenre;
import com.changa.book.mapper.BookMapper;
import com.changa.book.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/books")
public class BookController {

    private final BookService bookService;
    private final BookMapper bookMapper;

    public BookController(BookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }

    @PostMapping
    public ResponseEntity<BookDto> createBook(
            @Valid @RequestBody CreateBookRequestDto createBookRequestDto) {

        CreateBookRequest bookToCreate = bookMapper.fromDto(createBookRequestDto);

        Book createdBook = bookService.createBook(bookToCreate);

        BookDto createdBookDto = bookMapper.toDto(createdBook);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdBookDto);
    }

    @GetMapping
    public ResponseEntity<Page<BookDto>> listBooks(
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        Page<Book> books = bookService.listBooks(pageable);

        Page<BookDto> bookDtoList = books.map(bookMapper::toDto);

        return ResponseEntity.ok(bookDtoList);
    }

    @GetMapping(path = "/{bookId}")
    public ResponseEntity<BookDto> getBook(
            @PathVariable("bookId") UUID bookId) {

        Book foundBook = bookService.getBookById(bookId);

        BookDto bookDto = bookMapper.toDto(foundBook);

        return ResponseEntity.ok(bookDto);
    }

    @GetMapping(path = "/isbn/{bookIsbn}")
    public ResponseEntity<BookDto> getBookByIsbn(
            @PathVariable("bookIsbn") String bookIsbn) {

        Book foundBook = bookService.getBookByIsbn(bookIsbn);

        BookDto bookDto = bookMapper.toDto(foundBook);

        return ResponseEntity.ok(bookDto);
    }

    @GetMapping("/search/title")
    public ResponseEntity<Page<BookDto>> searchBooksByTitle(
            @RequestParam("title") String bookTitle,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        Page<Book> foundBooks = bookService.searchBooksByTitle(bookTitle, pageable);

        Page<BookDto> bookDtoList = foundBooks.map(bookMapper::toDto);

        return ResponseEntity.ok(bookDtoList);
    }

    @GetMapping("/search/author")
    public ResponseEntity<Page<BookDto>> searchBooksByAuthor(
            @RequestParam("author") String bookAuthor,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        Page<Book> foundBooks = bookService.searchBookByAuthor(bookAuthor, pageable);

        Page<BookDto> bookDtoList = foundBooks.map(bookMapper::toDto);

        return ResponseEntity.ok(bookDtoList);
    }

    @GetMapping("/search/genre")
    public ResponseEntity<Page<BookDto>> searchBooksByGenre(
            @RequestParam("genre")BookGenre genre,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        Page<Book> foundBooks = bookService.searchBooksByGenre(genre, pageable);

        Page<BookDto> bookDtoList = foundBooks.map(bookMapper::toDto);

        return ResponseEntity.ok(bookDtoList);
    }

    // Learn Spring Data JPA Specifications
    @GetMapping("/search/year")
    public ResponseEntity<Page<BookDto>> searchBooksByYearRange (
            @RequestParam("from") Integer from,
            @RequestParam("to") Integer to,
            @PageableDefault(size = 10, sort = "publicationYear") Pageable pageable) {

        Page<Book> books = bookService.searchBooksByYearRange(from, to, pageable);

        Page<BookDto> booksDto = books.map(bookMapper::toDto);

        return ResponseEntity.ok(booksDto);
    }

    @GetMapping("/authors")
    public ResponseEntity<List<String>> listAuthors() {
        return ResponseEntity.ok(bookService.listAuthors());
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<BookDto> updateBook(
            @PathVariable("bookId") UUID bookId,
            @Valid @RequestBody UpdateBookRequestDto updateBookRequestDto) {

        UpdateBookRequest bookToUpdate = bookMapper.fromDto(updateBookRequestDto);

        Book updatedBook = bookService.updateBook(bookId, bookToUpdate);

        BookDto updatedBookDto = bookMapper.toDto(updatedBook);

        return ResponseEntity.ok(updatedBookDto);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable("bookId") UUID bookId) {
        bookService.deleteBook(bookId);

        return ResponseEntity.noContent().build();
    }

}
