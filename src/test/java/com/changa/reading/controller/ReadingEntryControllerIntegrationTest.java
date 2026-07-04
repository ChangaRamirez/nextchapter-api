package com.changa.reading.controller;

import com.changa.auth.dto.RegisterRequestDto;
import com.changa.book.domain.dto.CreateBookRequestDto;
import com.changa.book.domain.entity.BookGenre;
import com.changa.book.repository.BookRepository;
import com.changa.reading.domain.dto.CreateReadingEntryRequestDto;
import com.changa.reading.domain.dto.UpdateReadingEntryNotesRequestDto;
import com.changa.reading.domain.dto.UpdateReadingEntryRequestDto;
import com.changa.reading.domain.entity.ReadingStatus;
import com.changa.reading.repository.ReadingEntryRepository;
import com.changa.support.BaseIntegrationTest;
import com.changa.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static com.changa.support.TestDataFactory.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReadingEntryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReadingEntryRepository readingEntryRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {

        readingEntryRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createReadingEntry_shouldReturnCreatedEntry_whenRequestIsValid() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(token, bookRequest);

        CreateReadingEntryRequestDto readingEntryRequest = defaultCreateReadingEntryRequest(bookId);

        mockMvc.perform(post("/api/v1/reading-entries")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(readingEntryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.bookTitle").value("Dune"))
                .andExpect(jsonPath("$.status").value("TO_READ"))
                .andExpect(jsonPath("$.rating").doesNotExist())
                .andExpect(jsonPath("$.startedAt").doesNotExist())
                .andExpect(jsonPath("$.finishedAt").doesNotExist());
    }

    @Test
    void createReadingEntry_shouldReturnBadRequest_whenFinishedHasNotFinishedAt() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(token, bookRequest);

        CreateReadingEntryRequestDto readingEntryRequest = new CreateReadingEntryRequestDto(
                bookId,
                ReadingStatus.FINISHED,
                5,
                LocalDate.parse("2025-01-01"),
                null
        );

        mockMvc.perform(post("/api/v1/reading-entries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(readingEntryRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("FINISHED")));
    }

    @Test
    void createReadingEntry_shouldReturnForbidden_whenTokenIsMissing() throws Exception{

        CreateReadingEntryRequestDto readingEntryRequest = defaultCreateReadingEntryRequest(UUID.fromString("a3c862a1-ca6f-48bb-8d2f-1ca6d4357f9c"));

        mockMvc.perform(post("/api/v1/reading-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(readingEntryRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createReadingEntry_shouldReturnConflict_whenUserAlreadyHasBook() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(token, bookRequest);

        CreateReadingEntryRequestDto readingEntryRequest = defaultCreateReadingEntryRequest(bookId);

        mockMvc.perform(post("/api/v1/reading-entries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(readingEntryRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/reading-entries")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(readingEntryRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(containsString("already exists")));
    }

    @Test
    void listReadingEntries_shouldReturnOnlyCurrentUserEntries() throws Exception {

        //Registering the first user
        RegisterRequestDto registerRequest = defaultRegisterRequest();

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated()).andReturn();

        String registerJsonContent = registerResult.getResponse().getContentAsString();

        JsonNode registerNode = jsonMapper.readTree(registerJsonContent);

        String tokenEduardo = registerNode.get("token").asText(); //This is the beautiful token extracted
        UUID idEduardo = UUID.fromString(registerNode.get("userId").asText()); //And this is the user's ID

        //Registering a second user
        String tokenAlice = registerAndGetToken(
                new RegisterRequestDto("Alice", "alice@test.com", "password123")
        );

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        CreateReadingEntryRequestDto readingEntryRequestEduardo = defaultCreateReadingEntryRequest(bookId);

        CreateReadingEntryRequestDto readingEntryRequestAlice = new CreateReadingEntryRequestDto(
                bookId,
                ReadingStatus.FINISHED,
                8,
                LocalDate.now().minus(10, ChronoUnit.MONTHS),
                LocalDate.now().minus(1, ChronoUnit.DAYS)
        );

        //Eduardo creates reading entry for a book
        mockMvc.perform(post("/api/v1/reading-entries")
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(readingEntryRequestEduardo)))
                .andExpect(status().isCreated());

        //Alice creates reading entry for the same book
        mockMvc.perform(post("/api/v1/reading-entries")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(readingEntryRequestAlice)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/reading-entries")
                .header("Authorization", "Bearer " + tokenEduardo)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.content[0].bookTitle").value("Dune"))
                .andExpect(jsonPath("$.content[0].userId").value(idEduardo.toString()))
                .andExpect(jsonPath("$.content[0].status").value("TO_READ"))
                .andExpect(jsonPath("$.content[0].rating").doesNotExist())
                .andExpect(jsonPath("$.content[0].startedAt").doesNotExist())
                .andExpect(jsonPath("$.content[0].finishedAt").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getReadingEntryById_shouldReturnNotFound_whenEntryBelongsToAnotherUser() throws Exception {

        //Registering the first user
        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        //Registering a second user
        String tokenAlice = registerAndGetToken(
                new RegisterRequestDto("Alice", "alice@test.com", "password123")
        );

        //Creating a book and storing its ID
        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        //Eduardo creates reading entry for a book and extract its ID
        CreateReadingEntryRequestDto readingEntryRequestEduardo = defaultCreateReadingEntryRequest(bookId);

        UUID readingEntryId = createReadingEntryAndReturnId(tokenEduardo, readingEntryRequestEduardo);

        //Alice tries to get Eduardo's Reading Entry
        mockMvc.perform(get("/api/v1/reading-entries/{readingEntryId}", readingEntryId)
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString(readingEntryId.toString())));
    }

    @Test
    void getReadingEntryById_shouldReturnReadingEntry_whenEntryBelongsToUser() throws Exception {

        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        CreateReadingEntryRequestDto readingEntryRequestEduardo = defaultCreateReadingEntryRequest(bookId);

        UUID readingEntryId = createReadingEntryAndReturnId(tokenEduardo, readingEntryRequestEduardo);

        mockMvc.perform(get("/api/v1/reading-entries/{readingEntryId}", readingEntryId)
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(bookId.toString()));

    }

    @Test
    void searchReadingEntries_shouldReturnReadingEntries_whenEntriesBelongToUser() throws Exception {

        //Registering the first user
        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        //Registering a second user
        String tokenAlice = registerAndGetToken(
                new RegisterRequestDto("Alice", "alice@test.com", "password123")
        );

        //Eduardo creates a book and we store its ID
        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        //Alice creates a book and we store its ID
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

        UUID secondBookId = createBookAndReturnId(tokenAlice, anotherBookRequest);

        //Eduardo creates reading entry for a book
        CreateReadingEntryRequestDto readingEntryRequestEduardo = defaultCreateReadingEntryRequest(bookId);

        mockMvc.perform(post("/api/v1/reading-entries")
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(readingEntryRequestEduardo)))
                .andExpect(status().isCreated());

        //Alice creates reading entry for a book
        CreateReadingEntryRequestDto readingEntryRequestAlice = defaultCreateReadingEntryRequest(secondBookId);

        mockMvc.perform(post("/api/v1/reading-entries")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(readingEntryRequestAlice)))
                .andExpect(status().isCreated());

        //Eduardo gets his own Reading Entry
        mockMvc.perform(get("/api/v1/reading-entries/recent")
                        .queryParam("days", "1")
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].bookId").value(bookId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        //Alice gets her own Reading Entry
        mockMvc.perform(get("/api/v1/reading-entries/recent")
                        .queryParam("days", "1")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].bookId").value(secondBookId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void searchReadingEntries_shouldReturnReadingEntries_whenEntriesFitParameter() throws Exception {

        //Registering the first user
        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        //Creating a first book and we store its ID
        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        //Creating a second book and we store its ID
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

        UUID secondBookId = createBookAndReturnId(tokenEduardo, anotherBookRequest);

        //Creating first reading entry at creation date now
        CreateReadingEntryRequestDto firstReadingEntryRequest = defaultCreateReadingEntryRequest(bookId);

        mockMvc.perform(post("/api/v1/reading-entries")
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(firstReadingEntryRequest)))
                .andExpect(status().isCreated());

        //Creating second reading entry at creation date 10 days before today
        CreateReadingEntryRequestDto secondReadingEntryRequest = defaultCreateReadingEntryRequest(secondBookId);

        UUID secondReadingEntryId = createReadingEntryAndReturnId(tokenEduardo, secondReadingEntryRequest);

        jdbcTemplate.update(
                "UPDATE reading_entry SET created = ? WHERE id = ?",
                Timestamp.from(Instant.now().minus(9, ChronoUnit.DAYS)),
                secondReadingEntryId
        );

        //Searching most recent Reading Entry
        mockMvc.perform(get("/api/v1/reading-entries/recent")
                        .queryParam("days", "1")
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        //Search includes older Reading Entry
        mockMvc.perform(get("/api/v1/reading-entries/recent")
                        .queryParam("days", "10")
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
}

    @Test
    void updateReadingEntry_shouldReturnNotFound_whenEntryBelongsToAnotherUser() throws Exception {

        //Registering the first user
        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        //Registering a second user
        String tokenAlice = registerAndGetToken(
                new RegisterRequestDto("Alice", "alice@test.com", "password123")
        );

        //Creating a book and storing its ID
        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        //Eduardo creates reading entry for a book and extract its ID
        CreateReadingEntryRequestDto readingEntryRequestEduardo = defaultCreateReadingEntryRequest(bookId);

        UUID readingEntryId = createReadingEntryAndReturnId(tokenEduardo, readingEntryRequestEduardo);

        //Alice tries to update Eduardo's Reading Entry
        UpdateReadingEntryRequestDto updateReadingEntryRequest = new UpdateReadingEntryRequestDto(
                ReadingStatus.FINISHED,
                8,
                LocalDate.now().minus(1, ChronoUnit.MONTHS),
                LocalDate.now().minus(1, ChronoUnit.DAYS)
        );

        mockMvc.perform(put("/api/v1/reading-entries/{readingEntryId}", readingEntryId)
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(updateReadingEntryRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString(readingEntryId.toString())));
    }

    @Test
    void updateReadingEntry_shouldReturnUpdatedEntry_whenEntryBelongsToCurrentUser() throws Exception {

        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        CreateReadingEntryRequestDto readingEntryRequestEduardo = defaultCreateReadingEntryRequest(bookId);

        UUID readingEntryId = createReadingEntryAndReturnId(tokenEduardo, readingEntryRequestEduardo);

        UpdateReadingEntryRequestDto updateReadingEntryRequest = new UpdateReadingEntryRequestDto(
                ReadingStatus.FINISHED,
                8,
                LocalDate.parse("2026-06-02"),
                LocalDate.parse("2026-07-01")
        );

        mockMvc.perform(put("/api/v1/reading-entries/{readingEntryId}", readingEntryId)
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateReadingEntryRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.rating").value(8))
                .andExpect(jsonPath("$.startedAt").value("2026-06-02"))
                .andExpect(jsonPath("$.finishedAt").value("2026-07-01"));
    }

    @Test
    void updateReadingEntryNotes_shouldReturnNotFound_whenEntryBelongsToAnotherUser() throws Exception {

        //Registering the first user
        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        //Registering a second user
        String tokenAlice = registerAndGetToken(
                new RegisterRequestDto("Alice", "alice@test.com", "password123")
        );

        //Creating a book and storing its ID
        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        //Eduardo creates reading entry for a book and extract its ID
        CreateReadingEntryRequestDto readingEntryRequestEduardo = defaultCreateReadingEntryRequest(bookId);

        UUID readingEntryId = createReadingEntryAndReturnId(tokenEduardo, readingEntryRequestEduardo);

        //Alice tries to update Eduardo's Reading Entry
        UpdateReadingEntryNotesRequestDto updateReadingEntryNotesRequest = new UpdateReadingEntryNotesRequestDto(
                null,
                "Must read this book before the summer ends"
        );

        mockMvc.perform(patch("/api/v1/reading-entries/{readingEntryId}/notes", readingEntryId)
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateReadingEntryNotesRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString(readingEntryId.toString())));
    }

    @Test
    void updateReadingEntryNotes_shouldReturnUpdatedEntry_whenEntryBelongsToCurrentUser() throws Exception {

        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        CreateReadingEntryRequestDto readingEntryRequestEduardo = defaultCreateReadingEntryRequest(bookId);

        UUID readingEntryId = createReadingEntryAndReturnId(tokenEduardo, readingEntryRequestEduardo);

        UpdateReadingEntryNotesRequestDto updateReadingEntryNotesRequest = new UpdateReadingEntryNotesRequestDto(
                null,
                "Must read this book before the summer ends"
        );

        mockMvc.perform(patch("/api/v1/reading-entries/{readingEntryId}/notes", readingEntryId)
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateReadingEntryNotesRequest)))
                .andExpect(status().isOk()).andDo(print())
                .andExpect(jsonPath("$.review").isEmpty())
                .andExpect(jsonPath("$.notes").value("Must read this book before the summer ends"));
    }

    @Test
    void updateReadingEntryNotes_shouldReturnBadRequest_whenReviewIsSubmittedButReadingStatusIsNotFit() throws Exception {

        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        CreateReadingEntryRequestDto readingEntryRequestEduardo = defaultCreateReadingEntryRequest(bookId);

        UUID readingEntryId = createReadingEntryAndReturnId(tokenEduardo, readingEntryRequestEduardo);

        UpdateReadingEntryNotesRequestDto updateReadingEntryNotesRequest = new UpdateReadingEntryNotesRequestDto(
                "Great book, would recommend",
                "Must read this book before the summer ends"
        );

        mockMvc.perform(patch("/api/v1/reading-entries/{readingEntryId}/notes", readingEntryId)
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateReadingEntryNotesRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateReadingEntryNotes_shouldReturnUpdatedReadingEntry_whenReviewIsSubmittedAndReadingStatusIsFit() throws Exception {

        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        CreateReadingEntryRequestDto readingEntryRequestEduardo = new CreateReadingEntryRequestDto(
                bookId,
                ReadingStatus.FINISHED,
                8,
                LocalDate.now().minus(1, ChronoUnit.DAYS),
                LocalDate.now()
        );

        UUID readingEntryId = createReadingEntryAndReturnId(tokenEduardo, readingEntryRequestEduardo);

        UpdateReadingEntryNotesRequestDto updateReadingEntryNotesRequest = new UpdateReadingEntryNotesRequestDto(
                "Great book, would recommend",
                "Must read this book before the summer ends"
        );

        mockMvc.perform(patch("/api/v1/reading-entries/{readingEntryId}/notes", readingEntryId)
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(updateReadingEntryNotesRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.review").value("Great book, would recommend"))
                .andExpect(jsonPath("$.notes").value("Must read this book before the summer ends"));
    }

    @Test
    void deleteReadingEntry_shouldReturnNotFound_whenReadingEntryBelongsToAnotherUser() throws Exception {

        //Registering the first user
        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        //Registering a second user
        String tokenAlice = registerAndGetToken(
                new RegisterRequestDto("Alice", "alice@test.com", "password123")
        );

        //Creating a book and storing its ID
        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        //Eduardo creates reading entry for a book and extract its ID
        CreateReadingEntryRequestDto readingEntryRequestEduardo = defaultCreateReadingEntryRequest(bookId);

        UUID readingEntryId = createReadingEntryAndReturnId(tokenEduardo, readingEntryRequestEduardo);

        //Alice tries to delete Eduardo's Reading Entry

        mockMvc.perform(delete("/api/v1/reading-entries/{readingEntryId}", readingEntryId)
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString(readingEntryId.toString())));
    }


    @Test
    void deleteReadingEntry_shouldReturnNotFound_whenReadingEntryDoesNotExist() throws Exception {

        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        UUID fakeReadingEntryId = UUID.fromString("b82780fe-c934-40e1-887e-9a0afa0dce91");

        mockMvc.perform(delete("/api/v1/reading-entries/{readingEntryId}", fakeReadingEntryId)
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString(fakeReadingEntryId.toString())));
    }

    @Test
    void deleteReadingEntry_shouldReturnNoReadingEntry_whenSearchingReadingEntryAfterDeletion() throws Exception {

        String tokenEduardo = registerAndGetToken(defaultRegisterRequest());

        CreateBookRequestDto bookRequest = defaultCreateBookRequest();

        UUID bookId = createBookAndReturnId(tokenEduardo, bookRequest);

        CreateReadingEntryRequestDto readingEntryRequestEduardo = defaultCreateReadingEntryRequest(bookId);

        UUID readingEntryId = createReadingEntryAndReturnId(tokenEduardo, readingEntryRequestEduardo);

        mockMvc.perform(delete("/api/v1/reading-entries/{readingEntryId}", readingEntryId)
                        .header("Authorization", "Bearer " + tokenEduardo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/reading-entries/{readingEntryId}", readingEntryId)
                .header("Authorization", "Bearer " + tokenEduardo)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}