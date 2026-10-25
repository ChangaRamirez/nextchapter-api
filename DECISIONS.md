# NextChapter — Architectural Decisions

This lightweight index records the nine decisions explicitly approved in NC-DOC-001. Recorded on 2026-10-25; original decision dates were not supplied and are not inferred from commit timestamps. Context and rationale below explain the approved choices, not an invented history of alternatives considered. Acceptance does not imply complete implementation.

Implementation references describe baseline `55bb9a60a0ad4ff3d83ff2940a613af86ee75828`. Broader ideas in [backend-v2-design.md](docs/backend-v2-design.md) retain Draft status. Commands, testing conventions, and transient progress belong in [AGENTS.md](AGENTS.md), [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md), and [CURRENT_STATUS.md](CURRENT_STATUS.md).

## ADR-001 — Modular-monolith backend

- **Status:** Accepted; implemented as one application with domain packages.
- **Context:** Authentication, catalog, and personal reading behavior need clear ownership inside this backend.
- **Decision:** Keep a single Spring Boot deployable organized by domain, with layers inside each domain.
- **Rationale:** Domain separation supports focused changes while retaining one build and runtime.
- **Consequences:** Package boundaries are conventions, not separately deployed or compiler-enforced modules. This decision does not authorize a microservice split.
- **References:** [POM](pom.xml), [application entry point](src/main/java/com/changa/BookApplication.java), [domain packages](src/main/java/com/changa/).

## ADR-002 — Shared catalog with administrator-managed writes

- **Status:** Accepted target; shared catalog implemented, role enforcement pending.
- **Context:** Book metadata is shared across users; a user's reading history is personal.
- **Decision:** Maintain one canonical catalog and restrict catalog writes to administrators in the target authorization model.
- **Rationale:** Shared metadata needs a different permission boundary from private reading activity.
- **Consequences:** NC-BE-002 must enforce roles; today any authenticated user can write books. Public/anonymous catalog reads are not approved by this decision. Role provisioning details require review.
- **References:** [Book](src/main/java/com/changa/book/domain/entity/Book.java), [UserRole](src/main/java/com/changa/user/domain/entity/UserRole.java), [SecurityConfig](src/main/java/com/changa/auth/config/SecurityConfig.java), [BookController](src/main/java/com/changa/book/controller/BookController.java).

## ADR-003 — Stable local Book identity; external provider adapters

- **Status:** Accepted; local identity/metadata implemented, provider adapters pending.
- **Context:** Reading entries need durable book references even when metadata originates elsewhere.
- **Decision:** Keep canonical Book records with stable local identifiers; external metadata providers act as adapters to that model.
- **Rationale:** Reading ownership and references should not depend on a provider's API or identifier scheme.
- **Consequences:** Provider metadata is distinct from the local UUID. Import, deduplication, refresh policy and provider selection still need design; no external lookup or synchronization currently exists. This does not adopt every flow in the draft design.
- **References:** [Book](src/main/java/com/changa/book/domain/entity/Book.java), [BookProvider](src/main/java/com/changa/book/domain/entity/BookProvider.java), [V3 metadata migration](src/main/resources/db/migration/V3__add_book_source_metadata.sql).

## ADR-004 — ReadingEntry owns ReadingNote lifecycle

- **Status:** Accepted; implemented.
- **Context:** A note belongs to a user's relationship with one book and cannot have an independent lifecycle.
- **Decision:** Treat ReadingEntry as the parent responsible for attaching and removing ReadingNote children; preserve the bidirectional relationship.
- **Rationale:** Lifecycle and ownership rules remain anchored to the reading entry.
- **Consequences:** Removing one note preserves the entry and siblings; removing the entry removes dependent notes. The child repository may support lookup but must not bypass the parent lifecycle. Successful note creation, update and deletion update the parent modification timestamp; rejected operations leave state unchanged.
- **References:** [ReadingEntry](src/main/java/com/changa/reading/domain/entity/ReadingEntry.java), [ReadingNoteServiceImpl](src/main/java/com/changa/reading/service/impl/ReadingNoteServiceImpl.java), [lifecycle integration tests](src/test/java/com/changa/reading/controller/ReadingNoteControllerIntegrationTest.java).

## ADR-005 — Multiple private notes per entry

- **Status:** Accepted; implemented.
- **Context:** Personal notes are distinct from the entry's overall review and can accumulate during reading.
- **Decision:** Support multiple private notes under each reading entry, rather than one text field.
- **Rationale:** Separate note records preserve individual content and timestamps within the user's reading history.
- **Consequences:** Responses include a note collection, currently ordered by creation time. Notes contain content, ID and timestamps; draft title/page/chapter/spoiler fields are not implemented or adopted here. The legacy note-string API is incompatible with the new collection; that integration change was explicitly accepted.
- **References:** [ReadingNote](src/main/java/com/changa/reading/domain/entity/ReadingNote.java), [ReadingEntryDto](src/main/java/com/changa/reading/domain/dto/ReadingEntryDto.java), [V4 extraction migration](src/main/resources/db/migration/V4__extract_note_entity.sql).

## ADR-006 — Managed JPA persistence for note mutations

- **Status:** Accepted; implemented for ReadingNote service mutations.
- **Context:** Child lifecycle changes must persist together with the parent's relationship and timestamp changes.
- **Decision:** Use transactions, managed entity changes, cascading persistence and orphan removal. Do not add unnecessary explicit saves to satisfy mock expectations.
- **Rationale:** JPA can persist the aggregate changes within the existing transaction boundary.
- **Consequences:** Dirty checking and both sides of the relationship matter. Creation currently flushes the parent repository; update/delete rely on managed changes. This does not assert that every service in the application has a service-level transaction. Database cascade SQL exists separately and still needs real MySQL verification.
- **References:** [ReadingNoteServiceImpl](src/main/java/com/changa/reading/service/impl/ReadingNoteServiceImpl.java), [ReadingEntry mapping](src/main/java/com/changa/reading/domain/entity/ReadingEntry.java), [note service tests](src/test/java/com/changa/reading/service/impl/ReadingNoteServiceImplTest.java).

## ADR-007 — Reject blank notes and preserve accepted content

- **Status:** Accepted; implemented at the HTTP DTO validation boundary.
- **Context:** A note must contain meaningful input without silently rewriting the user's text.
- **Decision:** Reject null, empty and whitespace-only content; retain the 5,000-character maximum; preserve accepted content exactly without trimming or normalization.
- **Rationale:** Validation and text preservation are compatible: invalid input is rejected, valid input is unchanged.
- **Consequences:** Create/update use `@NotBlank` and `@Length(max = 5000)`; the current length validator follows Java string length. Internal service request records do not independently enforce DTO validation. Rejected requests must not alter content, relationships or timestamps.
- **References:** [create DTO](src/main/java/com/changa/reading/domain/dto/CreateReadingNoteRequestDto.java), [update DTO](src/main/java/com/changa/reading/domain/dto/UpdateReadingNoteRequestDto.java), [validation and persistence tests](src/test/java/com/changa/reading/controller/ReadingNoteControllerIntegrationTest.java).

## ADR-008 — Ownership-scoped private reading resources

- **Status:** Accepted; implemented.
- **Context:** Authentication alone cannot authorize access to another user's reading history.
- **Decision:** Scope entry access to the authenticated owner and verify each note belongs to the addressed entry before mutation. Keep these reading resources private.
- **Rationale:** The server must enforce ownership rather than trusting user IDs or relationships supplied by clients.
- **Consequences:** Missing/inaccessible entry or note operations return JSON 404, without mutation. Lists/statistics are owner-scoped. Catalog administrator status must not implicitly grant access to private reading data. Public reviews/community features would require a separate decision.
- **References:** [ReadingEntryRepository](src/main/java/com/changa/reading/repository/ReadingEntryRepository.java), [ReadingNoteServiceImpl](src/main/java/com/changa/reading/service/impl/ReadingNoteServiceImpl.java), [exception handler](src/main/java/com/changa/exception/GlobalExceptionHandler.java), [two-user authorization tests](src/test/java/com/changa/reading/controller/ReadingNoteControllerIntegrationTest.java).

## ADR-009 — Human-reviewed AI-assisted engineering

- **Status:** Accepted; operational policy.
- **Context:** AI assistance must preserve human control over product/architecture decisions, existing work and Git publication boundaries.
- **Decision:** Work within explicit task scope and review gates. Preserve unrelated work; obtain authorization for commits, merges, pushes and rebases, and architectural review for security/data-integrity changes. Validate the exact staged snapshot for significant commit preparation.
- **Rationale:** Reviewable scope and reproducible evidence let humans assess what will actually enter history.
- **Consequences:** Read-only phases remain read-only. Drafts or test success are not implicit commit/push approval. Already-authorized actions need no duplicate permission request. Keep implemented behavior, accepted targets, proposals and test evidence distinct.
- **References:** Human approvals in NC-BE-001, local integration and NC-DOC-001; [AGENTS.md](AGENTS.md) operationalizes this policy. This decision is process policy, not a runtime security mechanism.
