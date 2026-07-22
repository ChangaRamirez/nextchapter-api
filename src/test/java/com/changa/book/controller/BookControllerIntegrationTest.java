package com.changa.book.controller;

import com.changa.book.domain.dto.CreateBookRequestDto;
import com.changa.book.domain.dto.UpdateBookRequestDto;
import com.changa.book.domain.entity.Book;
import com.changa.book.domain.entity.BookGenre;
import com.changa.book.domain.entity.BookProvider;
import com.changa.book.repository.BookRepository;
import com.changa.support.BaseIntegrationTest;
import com.changa.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Set;
import java.util.UUID;

import static com.changa.support.TestDataFactory.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

public class BookControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createBook_shouldReturnCreatedBook_whenRequestIsValid() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.provider").value("MANUAL"))
                .andExpect(jsonPath("$.title").value("Dune"))
                .andExpect(jsonPath("$.description").value("A sci-fi classic"))
                .andExpect(jsonPath("$.isbn").value("978-0441172719"))
                .andExpect(jsonPath("$.author").value("Frank Herbert"))
                .andExpect(jsonPath("$.genres").isArray())
                .andExpect(jsonPath("$.genres", hasItem("SCIENCE_FICTION")))
                .andExpect(jsonPath("$.genres", hasItem("ADVENTURE")))
                .andExpect(jsonPath("$.publicationYear").value(1965));

        Book savedBook = bookRepository.findByIsbn("978-0441172719")
                .orElseThrow();

        assertThat(savedBook.getProvider()).isEqualTo(BookProvider.MANUAL);
        assertThat(savedBook.getExternalId()).isNull();
        assertThat(savedBook.getCoverUrl()).isNull();
        assertThat(savedBook.getMetadataFetchedAt()).isNull();

    }

    @Test
    void createBook_shouldReturnUnauthorized_whenTokenIsMissing() throws Exception {

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Authentication required"));
    }

    @Test
    void createBook_shouldReturnConflict_whenIsbnAlreadyExists() throws Exception {

       String token = registerAndGetToken(defaultRegisterRequest());

       CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("already exists")));
    }

    @Test
    void listBooks_shouldReturnPagedBooks_whenAuthenticated() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();
        CreateBookRequestDto anotherBookRequest = new CreateBookRequestDto(
                "The Hobbit",
                "Bilbo Baggins embarks on an unexpected journey.",
                "978-0547928227",
                "J.R.R. Tolkien",
                Set.of(
                        BookGenre.FANTASY,
                        BookGenre.ADVENTURE
                ),
                1937
        );

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(anotherBookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].title", hasItem("Dune")))
                .andExpect(jsonPath("$.content[*].title", hasItem("The Hobbit")))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getBookById_shouldReturnBook_whenAuthenticated() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

      UUID bookId = createBookAndReturnId(token, bookRequest);

      mockMvc.perform(get("/api/v1/books/{id}", bookId)
              .header("Authorization", "Bearer " + token)
              .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(bookId.toString()))
              .andExpect(jsonPath("$.provider").value("MANUAL"))
              .andExpect(jsonPath("$.title").value("Dune"))
              .andExpect(jsonPath("$.description").value("A sci-fi classic"))
              .andExpect(jsonPath("$.isbn").value("978-0441172719"))
              .andExpect(jsonPath("$.author").value("Frank Herbert"))
              .andExpect(jsonPath("$.genres").isArray())
              .andExpect(jsonPath("$.genres", hasItem("SCIENCE_FICTION")))
              .andExpect(jsonPath("$.genres", hasItem("ADVENTURE")))
              .andExpect(jsonPath("$.publicationYear").value(1965));
    }

    @Test
    void getBookById_shouldReturnNotFound_whenBookDoesNotExist() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

       UUID nonExistentId = UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c");

        mockMvc.perform(get("/api/v1/books/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString(nonExistentId.toString())));
    }

    @Test
    void updateBook_shouldReturnUpdatedBook_whenRequestIsValid() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(token, bookRequest);

        UpdateBookRequestDto updateRequest = new UpdateBookRequestDto(
                "Dune Messiah",
                "A sci-fi sequel",
                "Frank Herbert",
                Set.of(
                        BookGenre.SCIENCE_FICTION,
                        BookGenre.ADVENTURE
                ),
                1969
        );

        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId.toString()))
                .andExpect(jsonPath("$.title").value("Dune Messiah"))
                .andExpect(jsonPath("$.description").value("A sci-fi sequel"))
                .andExpect(jsonPath("$.isbn").value("978-0441172719"))
                .andExpect(jsonPath("$.author").value("Frank Herbert"))
                .andExpect(jsonPath("$.genres").isArray())
                .andExpect(jsonPath("$.genres", hasItem("SCIENCE_FICTION")))
                .andExpect(jsonPath("$.genres", hasItem("ADVENTURE")))
                .andExpect(jsonPath("$.publicationYear").value(1969));

        Book updatedBook = bookRepository.findById(bookId)
                .orElseThrow();

        assertThat(updatedBook.getProvider()).isEqualTo(BookProvider.MANUAL);
        assertThat(updatedBook.getExternalId()).isNull();
        assertThat(updatedBook.getCoverUrl()).isNull();
        assertThat(updatedBook.getMetadataFetchedAt()).isNull();
    }

    @Test
    void updateBook_shouldReturnNotFound_whenBookDoesNotExist() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        UUID nonExistentId = UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c");

        UpdateBookRequestDto updateRequest = new UpdateBookRequestDto(
                "Dune Messiah",
                "A sci-fi sequel",
                "Frank Herbert",
                Set.of(
                        BookGenre.SCIENCE_FICTION,
                        BookGenre.ADVENTURE
                ),
                1969
        );

        mockMvc.perform(put("/api/v1/books/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString(nonExistentId.toString())));
    }

    @Test
    void deleteBook_shouldReturnNoBook_whenSearchingBookAfterDeletion() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(token, bookRequest);

        mockMvc.perform(delete("/api/v1/books/{id}", bookId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/v1/books/{id}", bookId)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                        .andExpect(jsonPath("$.error").value(containsString(bookId.toString())));

    }

    @Test
    void delete_shouldReturnNotFound_whenBookDoesNotExist() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        UUID nonExistentId = UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c");

        mockMvc.perform(delete("/api/v1/books/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString(nonExistentId.toString())));
    }

    @Test
    void searchBooks_shouldReturnBooks_whenTitleExists() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();
        CreateBookRequestDto anotherBookRequest = new CreateBookRequestDto(
                "The Hobbit",
                "Bilbo Baggins embarks on an unexpected journey.",
                "978-0547928227",
                "J.R.R. Tolkien",
                Set.of(
                        BookGenre.FANTASY,
                        BookGenre.ADVENTURE
                ),
                1937
        );
        CreateBookRequestDto aThirdBookRequest = new CreateBookRequestDto(
                "Dune Messiah",
                "The Dune saga continues",
                "978-0593098233",
                "Frank Herbert",
                Set.of(
                        BookGenre.FANTASY,
                        BookGenre.ADVENTURE
                ),
                1969
        );
        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(anotherBookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(aThirdBookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/books/search")
                        .queryParam("title", "dune")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].title", hasItem("Dune")))
                .andExpect(jsonPath("$.content[*].title", hasItem("Dune Messiah")))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void searchBooks_shouldReturnBooks_whenAuthorExists() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();
        CreateBookRequestDto anotherBookRequest = new CreateBookRequestDto(
                "The Hobbit",
                "Bilbo Baggins embarks on an unexpected journey.",
                "978-0547928227",
                "J.R.R. Tolkien",
                Set.of(
                        BookGenre.FANTASY,
                        BookGenre.ADVENTURE
                ),
                1937
        );
        CreateBookRequestDto aThirdBookRequest = new CreateBookRequestDto(
                "Dune Messiah",
                "The Dune saga continues",
                "978-0593098233",
                "Frank Herbert",
                Set.of(
                        BookGenre.FANTASY,
                        BookGenre.ADVENTURE
                ),
                1969
        );
        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(anotherBookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(aThirdBookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/books/search")
                        .queryParam("author", "tolkien")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].author", hasItem("J.R.R. Tolkien")))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
        void searchBooks_shouldReturnBooks_whenGenreExists() throws Exception {

            String token = registerAndGetToken(defaultRegisterRequest());

            CreateBookRequestDto bookRequest = defaultCreateBookRequest();
            CreateBookRequestDto anotherBookRequest = new CreateBookRequestDto(
                    "The Hobbit",
                    "Bilbo Baggins embarks on an unexpected journey.",
                    "978-0547928227",
                    "J.R.R. Tolkien",
                    Set.of(
                            BookGenre.FANTASY,
                            BookGenre.ADVENTURE
                    ),
                    1937
            );
            CreateBookRequestDto aThirdBookRequest = new CreateBookRequestDto(
                    "Dune Messiah",
                    "The Dune saga continues",
                    "978-0593098233",
                    "Frank Herbert",
                    Set.of(
                            BookGenre.FANTASY,
                            BookGenre.ADVENTURE
                    ),
                    1969
            );
            mockMvc.perform(post("/api/v1/books")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(bookRequest)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/books")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(anotherBookRequest)))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/v1/books")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonMapper.writeValueAsString(aThirdBookRequest)))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/v1/books/search")
                        .queryParam("genre", "ADVENTURE")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].genres").isArray())
                .andExpect(jsonPath("$.content[*].genres", everyItem(hasItem("ADVENTURE"))));

    }

    @Test
    void searchBooks_shouldReturnBooks_whenFiltersExist() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();
        CreateBookRequestDto anotherBookRequest = new CreateBookRequestDto(
                "The Hobbit",
                "Bilbo Baggins embarks on an unexpected journey.",
                "978-0547928227",
                "J.R.R. Tolkien",
                Set.of(
                        BookGenre.FANTASY,
                        BookGenre.ADVENTURE
                ),
                1937
        );
        CreateBookRequestDto aThirdBookRequest = new CreateBookRequestDto(
                "Dune Messiah",
                "The Dune saga continues",
                "978-0593098233",
                "Frank Herbert",
                Set.of(
                        BookGenre.FANTASY,
                        BookGenre.ADVENTURE,
                        BookGenre.ROMANCE
                ),
                1969
        );
        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(anotherBookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(aThirdBookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/books/search")
                        .queryParam("title", "dune")
                        .queryParam("author", "frank")
                        .queryParam("genre" ,"ROMANCE")
                        .queryParam("publication-year", "1969")
                        .queryParam("provider", "MANUAL")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].title", hasItem("Dune Messiah")))
                .andExpect(jsonPath("$.content[*].author", hasItem("Frank Herbert")))
                .andExpect(jsonPath("$.content[*].genres").isArray())
                .andExpect(jsonPath("$.content[*].genres", everyItem(hasItem("ROMANCE"))))
                .andExpect(jsonPath("$.content[*].publicationYear", hasItem(1969)))
                .andExpect(jsonPath("$.content[*].provider", everyItem(is("MANUAL"))));

    }

    @Test
    void searchBooks_shouldReturnManualBooks_whenProviderIsManual() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        createBookAndReturnId(token, defaultCreateBookRequest());

        mockMvc.perform(get("/api/v1/books/search")
                        .queryParam("provider", "MANUAL")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].provider").value("MANUAL"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchBooks_shouldReturnEmpty_whenFiltersDontExist() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();
        CreateBookRequestDto anotherBookRequest = new CreateBookRequestDto(
                "The Hobbit",
                "Bilbo Baggins embarks on an unexpected journey.",
                "978-0547928227",
                "J.R.R. Tolkien",
                Set.of(
                        BookGenre.FANTASY,
                        BookGenre.ADVENTURE
                ),
                1937
        );
        CreateBookRequestDto aThirdBookRequest = new CreateBookRequestDto(
                "Dune Messiah",
                "The Dune saga continues",
                "978-0593098233",
                "Frank Herbert",
                Set.of(
                        BookGenre.FANTASY,
                        BookGenre.ADVENTURE,
                        BookGenre.ROMANCE
                ),
                1969
        );
        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(anotherBookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(aThirdBookRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/books/search")
                        .queryParam("title", "harry potter")
                        .queryParam("author", "dickens")
                        .queryParam("genre", "ADVENTURE")
                        .queryParam("publication-year", "2003")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());

    }

    @Test
    void searchBooks_shouldThrowBadRequest_whenFiltersAreInvalid() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        mockMvc.perform(get("/api/v1/books/search")
                        .queryParam("title", "dune")
                        .queryParam("author", "frank")
                        .queryParam("genre", "COMEDY")
                        .queryParam("publication-year", "2030")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    void searchBooks_shouldReturnBadRequest_whenProviderIsInvalid() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        mockMvc.perform(get("/api/v1/books/search")
                        .queryParam("provider", "UNKNOWN_PROVIDER")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
