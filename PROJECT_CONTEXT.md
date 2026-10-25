# NextChapter — Project Context

Technical baseline: integration commit `55bb9a60a0ad4ff3d83ff2940a613af86ee75828`.
Use [DECISIONS.md](DECISIONS.md) for accepted architecture, [CURRENT_STATUS.md](CURRENT_STATUS.md) for dated progress and evidence, and [AGENTS.md](AGENTS.md) for working instructions. Recheck source when behavior changes.

## Purpose and repository boundary

NextChapter manages a person's reading experience around a shared book catalog. The backend currently provides registration/login, catalog CRUD and search, private reading entries, ratings, reviews, multiple private notes, and personal statistics through `/api/v1`.

This repository is one deployable Spring Boot application and one Maven module, organized by domain: a modular monolith, without separately compiled domain modules. The React/TypeScript frontend is a separate Git repository, found as sibling `frontend/` in the development workspace. It consumes the API and is not built by this Maven project.

## Stack and code map

The [POM](pom.xml) specifies Java 25, Spring Boot 4.0.7, Spring MVC/Security/Validation, Spring Data JPA with Hibernate, MySQL, Flyway, MapStruct 1.6.3, JJWT 0.12.7, and springdoc 3.0.1. The [Maven wrapper](.mvn/wrapper/maven-wrapper.properties) selects Maven 3.9.16. Lombok is not part of the committed baseline.

Packages are under `src/main/java/com/changa/`:

| Package | Responsibility |
| --- | --- |
| `auth` | Registration/login, JWT issuance/filtering, current-user access, security configuration |
| `user` | User entity, roles, repository |
| `book` | Shared catalog entities, DTOs, requests, controllers, services, mappers, repository, search specifications |
| `reading` | ReadingEntry and ReadingNote APIs, domain objects, persistence and mapping |
| `reading.statistics` | User-scoped counts and average rating |
| `exception`, `config` | HTTP error translation and OpenAPI configuration |

[BookApplication](src/main/java/com/changa/BookApplication.java) starts the application. There are no external catalog adapters yet.

## Domain and request flow

`User 1 → many ReadingEntry many → 1 Book`; `ReadingEntry 1 → many ReadingNote`. Entities use UUID identifiers. Book genres are an enum element collection; author is a string, not a separate entity.

[ReadingEntry](src/main/java/com/changa/reading/domain/entity/ReadingEntry.java) owns note lifecycle. Its bidirectional relationship uses `cascade = ALL`, `orphanRemoval = true`, and `created ASC` ordering. `addNote`/`removeNote` maintain both sides. Removing a note preserves its parent and siblings; deleting an entry removes its notes and preserves the shared book. This boundary is enforced by service conventions, not by immutable entities or an inaccessible child repository.

Requests pass through Spring Security, then controllers with validated DTOs. MapStruct maps DTOs to internal request records; services apply ownership/business rules and use Spring Data repositories. Controllers map returned entities to response DTOs. [GlobalExceptionHandler](src/main/java/com/changa/exception/GlobalExceptionHandler.java) translates handled failures to JSON `{ "error": "..." }` with appropriate HTTP status.

[ReadingNoteServiceImpl](src/main/java/com/changa/reading/service/impl/ReadingNoteServiceImpl.java) runs mutations transactionally. Creation attaches a note to a managed parent and flushes; updates use dirty checking; deletion removes the child through the parent. No explicit note `save()` is needed. Every successful note mutation updates `ReadingEntry.updated`; updating a note also updates its own timestamp. Rejected operations must leave content, relationships, and timestamps unchanged.

Note requests use `@NotBlank` and `@Length(max = 5000)` at the HTTP validation boundary. Accepted text is stored exactly, without trimming. Notes can accompany every reading status. Their content and timestamps are embedded in entry responses; there are no separate note GET endpoints. Mutations use POST `/api/v1/reading-entries/{readingEntryId}/notes` and PUT/DELETE on its `/{readingNoteId}` child path.

[ReadingEntryServiceImpl](src/main/java/com/changa/reading/service/impl/ReadingEntryServiceImpl.java) supports `TO_READ`, `READING`, `PAUSED`, `FINISHED`, and `ABANDONED`. Ratings are 0–10 and prohibited for `TO_READ`. Non-null reviews require `FINISHED` or `ABANDONED`; moving to another status clears the review. Reviews use PATCH `/api/v1/reading-entries/{readingEntryId}/review`. `FINISHED` requires a finish date; date validation rejects future dates and finish-before-start. Recent-entry search uses creation time. Duplicate user/book entries are checked in the service, without a matching database uniqueness constraint.

Catalog search combines title, author, genre, publication year, and provider filters, with pagination/sorting. Statistics report the user's total entries, finished/reading/to-read counts, and average non-null rating.

## Current security behavior

[SecurityConfig](src/main/java/com/changa/auth/config/SecurityConfig.java) permits authentication routes and Swagger/OpenAPI resources anonymously; all other routes, including catalog reads and writes, require authentication. Passwords use BCrypt. JWTs are HMAC-signed, identify users by email, and include user ID/name claims.

[JwtAuthenticationFilter](src/main/java/com/changa/auth/security/JwtAuthenticationFilter.java) loads the user and installs a principal with an empty authority list. `USER`/`ADMIN` roles are persisted, with `USER` the default, but role-based authorization is not enforced. Any authenticated user can currently mutate the catalog. Administrator-managed catalog writes are the accepted target, not current protection.

Reading operations resolve the authenticated user and use user-scoped entry queries. Note updates/deletions additionally check that the note belongs to that entry. Missing and inaccessible reading resources return 404; entry lists and statistics are user-scoped. Reviews are currently private through this same boundary, despite README wording.

CSRF is disabled. CORS permits `http://localhost:5173` with the configured methods and Authorization/Content-Type headers. Security configuration does not explicitly set stateless session management. The JWT filter does not locally translate token parsing exceptions; invalid/expired-token error behavior needs dedicated verification before claiming uniform authentication errors.

## Persistence and runtime

[Migrations](src/main/resources/db/migration/) define V1 initial schema; V2 user roles; V3 nullable ISBN and provider metadata with provider/external-ID uniqueness; V4 a separate note table, migration of nonblank legacy note text, and removal of the old column. V4 includes database `ON DELETE CASCADE`. Manual book creation still requires ISBN through its DTO even though the database permits null.

[Application properties](src/main/resources/application.properties) enable Flyway and default Hibernate to `validate`. Tests use a different schema strategy. Supply `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and `GOODREADS_JWT_SECRET` through the environment. Optional controls include `SERVER_PORT` (8080), `GOODREADS_JWT_EXPIRATION_MS` (3600000), `JPA_DDL_AUTO`, `SHOW_SQL`, `FLYWAY_ENABLED`, and `FLYWAY_BASELINE_ON_MIGRATE` (false).

[Compose](docker-compose.yml) runs MySQL 8.0 and the API, exposing database port 3307 and API port 8080, with a database health check and persistent volume. It reads environment substitutions from `.env`; direct Maven execution does not automatically load that file. Use `localhost:3307` for a host-run backend and `mysql:3306` inside Compose. The [Dockerfile](Dockerfile) builds with Java 25 JDK and runs on Java 25 JRE; its package step skips tests. `.env.example` currently selects `JPA_DDL_AUTO=update`, differing from the application default; review configuration before treating Flyway as the sole schema authority. Keep credentials out of Git and documentation.

## Verification and commands

Run from the backend root with Java 25 selected:

```sh
sh ./mvnw test -B -ntp
sh ./mvnw package
sh ./mvnw spring-boot:run
docker compose up --build
```

The first command runs the backend suite; package also runs tests. The last two need runtime configuration and a database. Use `sh` because the Unix wrapper is not executable in the committed tree.

Tests use JUnit, Mockito, AssertJ, Spring Boot and MockMvc. [BaseIntegrationTest](src/test/java/com/changa/support/BaseIntegrationTest.java) activates the test profile and uses real registration/JWTs for HTTP fixtures. [Test properties](src/test/resources/application-test.properties) use a shared named H2 database in MySQL compatibility mode, disable Flyway, and generate schema with Hibernate `create-drop`. Integration cleanup deletes dependent reading entries before users/books, with note cascades; note tests also delete notes explicitly. Passing these tests does not verify MySQL migrations, arbitrary test order, or parallel isolation.

## Accepted targets, proposals, and limitations

- **Accepted, partly implemented:** shared canonical local books with stable IDs and external providers as adapters; administrator-managed catalog writes. Local entities/metadata and roles exist; adapters and role enforcement do not. See ADR-002/003 in [DECISIONS.md](DECISIONS.md).
- **Exploratory:** public catalog routes, provider-specific import/synchronization flows, extra note fields, social features, goals and recommendations in [backend-v2-design.md](docs/backend-v2-design.md) remain draft unless separately approved. The draft is not an executable contract or a completed-work checklist.
- **Known limits:** mutable aggregate accessors, no database user/book uniqueness constraint, no demonstrated strictly monotonic timestamps under rapid/concurrent writes, and default Open EntityManager in View. Timestamp tests use deliberately older persisted values; equal creation timestamps have no declared secondary note ordering.
- **Evidence limits:** deployment claims in README are not independently verified here. [CURRENT_STATUS.md](CURRENT_STATUS.md) tracks test evidence and outstanding documentation conflicts; [SECURITY.md](SECURITY.md) remains the vulnerability-reporting policy.
