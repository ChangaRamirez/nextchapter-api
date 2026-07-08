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
import com.changa.exception.ErrorResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@SecurityRequirement(name = "bearerAuth")
@Tag(
        name = "Books",
        description = "Endpoints for creating, browsing, searching, updating, and deleting books."
)
@RestController
@RequestMapping(path = "/api/v1/books")
public class BookController {

    private final BookService bookService;
    private final BookMapper bookMapper;

    public BookController(BookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }

    @Operation(
            summary = "Create a new book",
            description = "Creates a new book in the catalog. ISBN must be unique."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Book created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Book with the same ISBN already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
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

    @Operation(
            summary = "List books",
            description = "Returns a paginated list of books."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Books retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @Parameters({
            @Parameter(
                    name = "page",
                    description = "Zero-based page index.",
                    example = "0"
            ),
            @Parameter(
                    name = "size",
                    description = "Number of books returned per page.",
                    example = "10"
            ),
            @Parameter(
                    name = "sort",
                    description = "Sorting criteria in the format: property,direction. Example: title,asc",
                    example = "title,asc"
            )
    })
    @GetMapping
    public ResponseEntity<Page<BookDto>> listBooks(
            @Parameter(hidden = true)
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {
        Page<Book> books = bookService.listBooks(pageable);

        Page<BookDto> bookDtoList = books.map(bookMapper::toDto);

        return ResponseEntity.ok(bookDtoList);
    }

    @Operation(
            summary = "Get book by its ID",
            description = "Retrieves a book using its unique identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Book retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid book ID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book with the given ID was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @GetMapping(path = "/{bookId}")
    public ResponseEntity<BookDto> getBookById(
            @Parameter(
                    description = "Book unique identifier.",
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable("bookId") UUID bookId) {

        Book foundBook = bookService.getBookById(bookId);

        BookDto bookDto = bookMapper.toDto(foundBook);

        return ResponseEntity.ok(bookDto);
    }

    @Operation(
            summary = "Get book by its ISBN",
            description = "Retrieves a book using its unique International Standard Book Number."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Book retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book with the given ISBN was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @GetMapping(path = "/isbn/{bookIsbn}")
    public ResponseEntity<BookDto> getBookByIsbn(
            @Parameter(
                    description = "International Standard Book Number (ISBN) of the book.",
                    example = "9780547928227"
            )
            @PathVariable("bookIsbn") String bookIsbn) {

        Book foundBook = bookService.getBookByIsbn(bookIsbn);

        BookDto bookDto = bookMapper.toDto(foundBook);

        return ResponseEntity.ok(bookDto);
    }

    @Operation(
            summary = "Search books",
            description = "Searches books using optional filters. Filters can be combined."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book retrieved successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid search parameter",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @GetMapping("/search")
    public ResponseEntity<Page<BookDto>> searchBooks(

            @Parameter(description = "Optional title filter.", example = "The Hobbit")
            @RequestParam(required = false) String title,

            @Parameter(description = "Optional author filter.", example = "J. R. R. Tolkien")
            @RequestParam(required = false) String author,

            @Parameter(description = "Optional genre filter.", example = "ADVENTURE")
            @RequestParam(required = false) BookGenre genre,

            @Parameter(description = "Optional publication year filter", example = "1937")
            @RequestParam(required = false) Integer publicationYear,

            @Parameter(hidden = true)
            @PageableDefault(value = 10, sort = "title") Pageable pageable) {

        Page<Book> books = bookService.searchBooks(title, author, genre, publicationYear, pageable);

        return ResponseEntity.ok(books.map(bookMapper::toDto));
    }

    @Operation(
            summary = "List authors",
            description = "Returns all authors available in the book catalog, sorted alphabetically."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authors retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @GetMapping("/authors")
    public ResponseEntity<List<String>> listAuthors() {
        return ResponseEntity.ok(bookService.listAuthors());
    }

    @Operation(
            summary = "Update book",
            description = "Updates a book's title, description, author, genres, and publication year."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Book updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid book ID format or request body",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book with the given ID was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @PutMapping("/{bookId}")
    public ResponseEntity<BookDto> updateBook(
            @Parameter(
                    description = "Book unique identifier.",
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable("bookId") UUID bookId,

            @Valid @RequestBody UpdateBookRequestDto updateBookRequestDto) {

        UpdateBookRequest bookToUpdate = bookMapper.fromDto(updateBookRequestDto);

        Book updatedBook = bookService.updateBook(bookId, bookToUpdate);

        BookDto updatedBookDto = bookMapper.toDto(updatedBook);

        return ResponseEntity.ok(updatedBookDto);
    }

    @Operation(
            summary = "Delete book",
            description = "Deletes a book from the catalog."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Book deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid book ID format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book with the given ID was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    })
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(
            @Parameter(
                    description = "Book unique identifier.",
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"
            )
            @PathVariable("bookId") UUID bookId) {
        bookService.deleteBook(bookId);

        return ResponseEntity.noContent().build();
    }

}
