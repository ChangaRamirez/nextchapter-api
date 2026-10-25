package com.changa.reading.controller;

import com.changa.auth.dto.RegisterRequestDto;
import com.changa.book.domain.dto.CreateBookRequestDto;
import com.changa.reading.domain.entity.ReadingStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;
import com.changa.book.repository.BookRepository;
import com.changa.reading.domain.dto.CreateReadingEntryRequestDto;
import com.changa.reading.domain.dto.CreateReadingNoteRequestDto;
import com.changa.reading.domain.entity.ReadingEntry;
import com.changa.reading.domain.entity.ReadingNote;
import com.changa.reading.repository.ReadingEntryRepository;
import com.changa.reading.repository.ReadingNoteRepository;
import com.changa.support.BaseIntegrationTest;
import com.changa.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static com.changa.support.TestDataFactory.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReadingNoteControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReadingEntryRepository readingEntryRepository;

    @Autowired
    private ReadingNoteRepository readingNoteRepository;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {

        readingNoteRepository.deleteAll();
        readingEntryRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createReadingNote_shouldReturnCreatedNote_whenRequestIsValid() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());
        CreateBookRequestDto bookRequest = defaultCreateBookRequest();
        UUID bookId = createBookAndReturnId(token, bookRequest);
        CreateReadingEntryRequestDto readingEntryRequest = defaultCreateReadingEntryRequest(bookId);
        UUID readingEntryId = createReadingEntryAndReturnId(token, readingEntryRequest);

        CreateReadingNoteRequestDto readingNoteRequest = defaultCreateReadingNoteRequest();

        mockMvc.perform(post("/api/v1/reading-entries/{readingEntryId}/notes", readingEntryId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(readingNoteRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.content").value("Must finish this book before the summer ends."));

        List<ReadingNote> readingNotes = readingNoteRepository.findAll();

        assertThat(readingNotes).hasSize(1);
        assertThat(readingNotes.getFirst().getId()).isNotNull();
        assertThat(readingNotes.getFirst().getReadingEntry().getId()).isEqualTo(readingEntryId);
    }

    @Test
    void deleteReadingNote_shouldReturnNoContent_whenNoteBelongsToReadingEntry() throws Exception {

        String token = registerAndGetToken(defaultRegisterRequest());

        UUID bookId = createBookAndReturnId(token, defaultCreateBookRequest());

        UUID readingEntryId = createReadingEntryAndReturnId(token, defaultCreateReadingEntryRequest(bookId));

        UUID readingNoteId = createReadingNoteAndReturnId(token, readingEntryId, defaultCreateReadingNoteRequest());

        assertThat(readingNoteRepository.findById(readingNoteId))
                .isPresent();

        mockMvc.perform(delete(
                "/api/v1/reading-entries/{readingEntryId}/notes/{readingNoteId}",
                readingEntryId,
                readingNoteId
        )
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(readingNoteRepository.findById(readingNoteId))
                .isEmpty();

        assertThat(readingEntryRepository.findById(readingEntryId))
                .isPresent();
    }

    private static final Instant OLD_UPDATED = Instant.parse("2020-01-01T00:00:00Z");

    @ParameterizedTest
    @MethodSource("acceptedContents")
    void createAndUpdate_shouldPreserveExactContentAndPersistTimestamps(String content) throws Exception {
        Fixture fixture = fixture();
        setEntryUpdated(fixture.entryId(), OLD_UPDATED);
        DatabaseState before = databaseState();

        JsonNode created = responseBody(mockMvc.perform(noteRequest("CREATE", fixture.entryId(), null, content)
                        .header("Authorization", "Bearer " + fixture.token()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.created").isNotEmpty())
                .andExpect(jsonPath("$.updated").isNotEmpty())
                .andReturn().getResponse().getContentAsString());
        UUID noteId = UUID.fromString(created.get("id").asText());
        ReadingNote saved = readingNoteRepository.findById(noteId).orElseThrow();
        assertThat(saved.getContent()).isEqualTo(content);
        assertThat(saved.getReadingEntry().getId()).isEqualTo(fixture.entryId());
        assertThat(saved.getCreated()).isCloseTo(Instant.parse(created.get("created").asText()),
                within(1, java.time.temporal.ChronoUnit.MICROS));
        assertThat(saved.getUpdated()).isCloseTo(Instant.parse(created.get("updated").asText()),
                within(1, java.time.temporal.ChronoUnit.MICROS));
        assertThat(readingEntryRepository.findById(fixture.entryId()).orElseThrow().getUpdated())
                .isAfter(OLD_UPDATED);
        assertThat(databaseState().notes()).hasSize(before.notes().size() + 1);

        Instant createdAt = saved.getCreated();
        jdbcTemplate.update("UPDATE reading_note SET updated = ? WHERE id = ?", Timestamp.from(OLD_UPDATED), noteId);
        setEntryUpdated(fixture.entryId(), OLD_UPDATED);
        // Use a different valid value, including significant surrounding whitespace.
        String updatedContent = content.equals("x".repeat(5000)) ? "y".repeat(5000) : "\n " + content + "\t ";
        mockMvc.perform(noteRequest("UPDATE", fixture.entryId(), noteId, updatedContent)
                        .header("Authorization", "Bearer " + fixture.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(noteId.toString()))
                .andExpect(jsonPath("$.content").value(updatedContent));

        ReadingNote updated = readingNoteRepository.findById(noteId).orElseThrow();
        assertThat(updated.getContent()).isEqualTo(updatedContent);
        assertThat(updated.getCreated()).isEqualTo(createdAt);
        assertThat(updated.getUpdated()).isAfter(OLD_UPDATED);
        assertThat(updated.getReadingEntry().getId()).isEqualTo(fixture.entryId());
        assertThat(readingEntryRepository.findById(fixture.entryId()).orElseThrow().getUpdated())
                .isAfter(OLD_UPDATED);
        mockMvc.perform(get("/api/v1/reading-entries/{id}", fixture.entryId())
                        .header("Authorization", "Bearer " + fixture.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes[?(@.id == '" + noteId + "')].content")
                        .value(org.hamcrest.Matchers.contains(updatedContent)));
    }

    static Stream<String> acceptedContents() {
        return Stream.of("  A thought.\n\tKeep this whitespace.  ", "x".repeat(5000), "Café — 読書 📖");
    }

    @ParameterizedTest
    @MethodSource("invalidContents")
    void createAndUpdate_shouldRejectInvalidContent_withoutChangingState(String operation, String content) throws Exception {
        Fixture fixture = fixture();
        DatabaseState before = databaseState();

        mockMvc.perform(noteRequest(operation, fixture.entryId(), fixture.noteId(), content)
                        .header("Authorization", "Bearer " + fixture.token()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").isString());

        assertThat(databaseState()).isEqualTo(before);
    }

    static Stream<Arguments> invalidContents() {
        return Stream.of("CREATE", "UPDATE").flatMap(operation ->
                Stream.of(null, "", " \t\r\n ", "x".repeat(5001))
                        .map(content -> Arguments.of(operation, content)));
    }

    @ParameterizedTest
    @MethodSource("rejectedOperations")
    void mutations_shouldReturnJson404_withoutChangingState(String operation, String reason) throws Exception {
        Fixture fixture = fixture();
        UUID entryId = fixture.entryId();
        UUID noteId = fixture.noteId();
        boolean missingEntry = reason.startsWith("ENTRY_");
        if (reason.equals("ENTRY_MISSING")) {
            entryId = UUID.randomUUID();
        } else if (reason.equals("ENTRY_FOREIGN") || reason.equals("NOTE_FOREIGN")) {
            String otherToken = registerAndGetToken(new RegisterRequestDto("Other reader", "other@test.com", "password123"));
            UUID otherEntry = createReadingEntryAndReturnId(otherToken, defaultCreateReadingEntryRequest(fixture.bookId()));
            UUID otherNote = createReadingNoteAndReturnId(otherToken, otherEntry, defaultCreateReadingNoteRequest());
            if (reason.equals("ENTRY_FOREIGN")) {
                entryId = otherEntry;
                noteId = otherNote;
            } else {
                noteId = otherNote;
            }
        } else if (reason.equals("NOTE_WRONG_ENTRY")) {
            CreateBookRequestDto book = defaultCreateBookRequest();
            UUID otherBook = createBookAndReturnId(fixture.token(), new CreateBookRequestDto(
                    "Another book", book.description(), "978-0547928227", book.author(), book.genres(), book.publicationYear()));
            UUID otherEntry = createReadingEntryAndReturnId(fixture.token(), defaultCreateReadingEntryRequest(otherBook));
            noteId = createReadingNoteAndReturnId(fixture.token(), otherEntry, defaultCreateReadingNoteRequest());
        } else {
            noteId = UUID.randomUUID();
        }
        DatabaseState before = databaseState();
        String expectedError = (missingEntry ? "Reading Entry" : "Reading Note")
                + " with ID '" + (missingEntry ? entryId : noteId) + "' does not exist";

        mockMvc.perform(noteRequest(operation, entryId, noteId, "Should never be stored")
                        .header("Authorization", "Bearer " + fixture.token()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value(expectedError));

        assertThat(databaseState()).isEqualTo(before);
    }

    static Stream<Arguments> rejectedOperations() {
        return Stream.of("CREATE", "UPDATE", "DELETE").flatMap(operation -> {
            Stream<String> reasons = operation.equals("CREATE")
                    ? Stream.of("ENTRY_MISSING", "ENTRY_FOREIGN")
                    : Stream.of("ENTRY_MISSING", "ENTRY_FOREIGN", "NOTE_MISSING", "NOTE_WRONG_ENTRY", "NOTE_FOREIGN");
            return reasons.map(reason -> Arguments.of(operation, reason));
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE", "UPDATE", "DELETE"})
    void mutations_shouldRequireAuthentication_withoutChangingState(String operation) throws Exception {
        Fixture fixture = fixture();
        DatabaseState before = databaseState();
        mockMvc.perform(noteRequest(operation, fixture.entryId(), fixture.noteId(), "A thought"))
                .andExpect(status().isUnauthorized());
        assertThat(databaseState()).isEqualTo(before);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CREATE", "UPDATE"})
    void mutations_shouldRejectMalformedJson_withoutChangingState(String operation) throws Exception {
        Fixture fixture = fixture();
        DatabaseState before = databaseState();
        mockMvc.perform(noteRequest(operation, fixture.entryId(), fixture.noteId(), "A thought")
                        .content("{\"content\":")
                        .header("Authorization", "Bearer " + fixture.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString());
        assertThat(databaseState()).isEqualTo(before);
    }

    @ParameterizedTest
    @MethodSource("invalidIds")
    void mutations_shouldRejectInvalidIds_withoutChangingState(String operation, boolean invalidEntry) throws Exception {
        Fixture fixture = fixture();
        DatabaseState before = databaseState();
        String path = "/api/v1/reading-entries/" + (invalidEntry ? "invalid" : fixture.entryId()) + "/notes";
        if (!operation.equals("CREATE")) {
            path += "/" + (invalidEntry ? fixture.noteId() : "invalid");
        }
        MockHttpServletRequestBuilder request = switch (operation) {
            case "CREATE" -> post(path);
            case "UPDATE" -> put(path);
            default -> delete(path);
        };
        mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON).content("{\"content\":\"A thought\"}")
                        .header("Authorization", "Bearer " + fixture.token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").isString());
        assertThat(databaseState()).isEqualTo(before);
    }

    static Stream<Arguments> invalidIds() {
        return Stream.of(Arguments.of("CREATE", true), Arguments.of("UPDATE", true),
                Arguments.of("UPDATE", false), Arguments.of("DELETE", true), Arguments.of("DELETE", false));
    }

    @ParameterizedTest
    @EnumSource(ReadingStatus.class)
    void createReadingNote_shouldAllowEveryReadingStatus(ReadingStatus readingStatus) throws Exception {
        String token = registerAndGetToken(defaultRegisterRequest());
        UUID bookId = createBookAndReturnId(token, defaultCreateBookRequest());
        UUID entryId = createReadingEntryAndReturnId(token, new CreateReadingEntryRequestDto(
                bookId, readingStatus, null, null,
                readingStatus == ReadingStatus.FINISHED ? LocalDate.of(2020, 1, 1) : null));
        UUID noteId = createReadingNoteAndReturnId(token, entryId, defaultCreateReadingNoteRequest());
        assertThat(readingNoteRepository.findById(noteId).orElseThrow().getReadingEntry().getId()).isEqualTo(entryId);
    }

    @Test
    void deleteReadingNote_shouldAdvanceParentTimestamp_andPreserveSibling() throws Exception {
        Fixture fixture = fixture();
        setEntryUpdated(fixture.entryId(), OLD_UPDATED);
        Map<String, Object> siblingBefore = jdbcTemplate.queryForMap("SELECT * FROM reading_note WHERE id = ?", fixture.siblingId());

        mockMvc.perform(noteRequest("DELETE", fixture.entryId(), fixture.noteId(), null)
                        .header("Authorization", "Bearer " + fixture.token()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(readingNoteRepository.findById(fixture.noteId())).isEmpty();
        assertThat(readingEntryRepository.findById(fixture.entryId()).orElseThrow().getUpdated()).isAfter(OLD_UPDATED);
        assertThat(jdbcTemplate.queryForMap("SELECT * FROM reading_note WHERE id = ?", fixture.siblingId()))
                .isEqualTo(siblingBefore);
        mockMvc.perform(get("/api/v1/reading-entries/{id}", fixture.entryId())
                        .header("Authorization", "Bearer " + fixture.token()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notes.length()").value(1))
                .andExpect(jsonPath("$.notes[0].id").value(fixture.siblingId().toString()));
    }

    @Test
    void entryResponses_shouldMapAllNoteFields_inCreationOrder() throws Exception {
        Fixture fixture = fixture();
        // Reverse chronological order relative to insertion to test the database ordering.
        Instant earlier = Instant.parse("2020-01-01T00:00:00Z");
        Instant later = earlier.plusSeconds(60);
        jdbcTemplate.update("UPDATE reading_note SET created = ?, updated = ? WHERE id = ?",
                Timestamp.from(later), Timestamp.from(later), fixture.noteId());
        jdbcTemplate.update("UPDATE reading_note SET created = ?, updated = ? WHERE id = ?",
                Timestamp.from(earlier), Timestamp.from(earlier), fixture.siblingId());
        for (String path : List.of("/api/v1/reading-entries/" + fixture.entryId(),
                "/api/v1/reading-entries", "/api/v1/reading-entries/recent?days=30")) {
            String prefix = path.endsWith(fixture.entryId().toString()) ? "$" : "$.content[0]";
            mockMvc.perform(get(path).header("Authorization", "Bearer " + fixture.token()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(prefix + ".notes.length()").value(2))
                    .andExpect(jsonPath(prefix + ".notes[0].id").value(fixture.siblingId().toString()))
                    .andExpect(jsonPath(prefix + ".notes[0].content").value("Sibling note"))
                    .andExpect(jsonPath(prefix + ".notes[0].created").value(earlier.toString()))
                    .andExpect(jsonPath(prefix + ".notes[0].updated").value(earlier.toString()))
                    .andExpect(jsonPath(prefix + ".notes[1].id").value(fixture.noteId().toString()))
                    .andExpect(jsonPath(prefix + ".notes[1].content").value("Original note"));
        }
    }

    @Test
    void deleteReadingEntry_shouldDeleteDependentNotes_andPreserveBookAndOtherEntry() throws Exception {
        Fixture fixture = fixture();
        String otherToken = registerAndGetToken(new RegisterRequestDto("Other reader", "other@test.com", "password123"));
        UUID otherEntry = createReadingEntryAndReturnId(otherToken, defaultCreateReadingEntryRequest(fixture.bookId()));
        UUID otherNote = createReadingNoteAndReturnId(otherToken, otherEntry, defaultCreateReadingNoteRequest());
        Map<String, Object> otherBefore = jdbcTemplate.queryForMap("SELECT * FROM reading_note WHERE id = ?", otherNote);

        mockMvc.perform(delete("/api/v1/reading-entries/{id}", fixture.entryId())
                        .header("Authorization", "Bearer " + fixture.token()))
                .andExpect(status().isNoContent());

        assertThat(readingEntryRepository.findById(fixture.entryId())).isEmpty();
        assertThat(readingNoteRepository.findById(fixture.noteId())).isEmpty();
        assertThat(readingNoteRepository.findById(fixture.siblingId())).isEmpty();
        assertThat(bookRepository.findById(fixture.bookId())).isPresent();
        assertThat(readingEntryRepository.findById(otherEntry)).isPresent();
        assertThat(jdbcTemplate.queryForMap("SELECT * FROM reading_note WHERE id = ?", otherNote)).isEqualTo(otherBefore);
    }

    private Fixture fixture() throws Exception {
        String token = registerAndGetToken(defaultRegisterRequest());
        UUID bookId = createBookAndReturnId(token, defaultCreateBookRequest());
        UUID entryId = createReadingEntryAndReturnId(token, defaultCreateReadingEntryRequest(bookId));
        UUID noteId = createReadingNoteAndReturnId(token, entryId, new CreateReadingNoteRequestDto("Original note"));
        UUID siblingId = createReadingNoteAndReturnId(token, entryId, new CreateReadingNoteRequestDto("Sibling note"));
        return new Fixture(token, bookId, entryId, noteId, siblingId);
    }

    private MockHttpServletRequestBuilder noteRequest(String operation, UUID entryId, UUID noteId, String noteContent) {
        String path = "/api/v1/reading-entries/" + entryId + "/notes";
        MockHttpServletRequestBuilder request = switch (operation) {
            case "CREATE" -> post(path);
            case "UPDATE" -> put(path + "/" + noteId);
            case "DELETE" -> delete(path + "/" + noteId);
            default -> throw new IllegalArgumentException("Unknown note operation: " + operation);
        };
        return operation.equals("DELETE") ? request : request.contentType(MediaType.APPLICATION_JSON)
                .content(jsonMapper.writeValueAsString(new CreateReadingNoteRequestDto(noteContent)));
    }

    private JsonNode responseBody(String json) {
        return jsonMapper.readTree(json);
    }

    private void setEntryUpdated(UUID entryId, Instant updated) {
        jdbcTemplate.update("UPDATE reading_entry SET updated = ? WHERE id = ?", Timestamp.from(updated), entryId);
    }

    private DatabaseState databaseState() {
        return new DatabaseState(jdbcTemplate.queryForList("SELECT * FROM reading_entry ORDER BY id"),
                jdbcTemplate.queryForList("SELECT * FROM reading_note ORDER BY id"));
    }

    private record Fixture(String token, UUID bookId, UUID entryId, UUID noteId, UUID siblingId) {}
    private record DatabaseState(List<Map<String, Object>> entries, List<Map<String, Object>> notes) {}
}
