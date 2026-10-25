# NextChapter — Current Status

Status date: **2026-10-25**. This report covers the backend baseline; update it when integration state or verification evidence changes.

## Integration and completed work

- Published baseline and synchronized local/remote `main`: `f4c454b51e45a02f202ec9715241450eb98f9ce5`.
- Verified tree: `4a612d27f40a657bc889c150fe6940c5372d9fcd`.
- **NC-BE-001 completed and integrated:** four feature commits introduced user roles, catalog metadata, ReadingNote extraction, and the completed private note lifecycle, ending at `55bb9a60a0ad4ff3d83ff2940a613af86ee75828`.
- **NC-DOC-001 completed:** the four knowledge documents were architecturally reviewed, committed as `f4c454b`, integrated into `main`, and published to GitHub.
- During NC-DOC-002, a live `git ls-remote` check confirmed GitHub's `main` at the published baseline in `ChangaRamirez/nextchapter-api`; local `main` and `origin/main` match. The original feature branch remains one commit ahead of its remote-tracking counterpart; its completed work is already included in published `main`.

The original feature worktree retains an unrelated unstaged Lombok addition in `pom.xml`, untracked `.stignore`, the historical `CURRENT_STATUS.md`, and eight untracked `.DS_Store` files. These changes remain preserved and excluded from the published baseline and this handoff update. The historical status report has not been overwritten; this tracked report is being updated in an isolated worktree.

## Implemented capabilities

Registration/login with JWT and BCrypt; authenticated shared catalog CRUD, ISBN lookup, author listing and combined filters; owner-scoped reading entries with statuses, dates, ratings, reviews, recent lookup and statistics; multiple private notes with creation-order mapping, persistence and parent-managed deletion. See [PROJECT_CONTEXT.md](PROJECT_CONTEXT.md) for contracts and source references.

NC-BE-001 fixed note JSON 404 handling, parent timestamps on note deletion, blank-content rejection without text normalization, stale unit-test save expectations, and foreign-key cleanup failures in auth/book integration tests. Coverage now includes ownership, rejection without mutation, validation, persistence, mapping/order, sibling preservation and parent deletion. These former note defects are resolved in the verified baseline.

## Test evidence and limits

**Last backend validation:** `sh ./mvnw test -B -ntp` ran against approved code baseline `55bb9a60a0ad4ff3d83ff2940a613af86ee75828` in the isolated local-main integration worktree on Java 25 (Temurin 25.0.3): **135 tests, 0 failures, 0 errors, 0 skips**, across 10 suites. NC-DOC-001 changed documentation only, and no new backend test run followed those changes or was performed for NC-DOC-002. Existing Surefire XML totals were rechecked during NC-DOC-001 drafting. Earlier feature validation also included two successful complete-suite runs and exact staged-snapshot testing.

- H2 tests disable Flyway and use Hibernate-generated `create-drop` schema in MySQL compatibility mode. No real MySQL integration test has verified Flyway migrations, including V4 data conversion and database cascade behavior.
- Repeated sequential passes do not prove arbitrary test-order or parallel-execution independence. Integration classes share a named database; cleanup respects foreign-key dependencies but parallel isolation remains unverified.
- Note timestamp tests use deliberately older stored values; they do not establish strict monotonicity under clock rollback, timestamp precision limits or concurrent writes.
- Prior run warnings included deprecated/unchecked test APIs, Mockito dynamic agent attachment and JVM class-sharing warnings, default Open EntityManager in View, generated development security password, unstable `PageImpl` serialization, and enabled API documentation. Warning removal was outside NC-BE-001.
- No backend tests, containers, migrations, frontend build or deployment checks were executed for this documentation-only task.

## Pending work and risks

1. **Next recommended engineering task: NC-BE-002 — Role-Based Catalog Authorization.** `USER`/`ADMIN` roles exist, but the JWT principal has no authorities and catalog writes require only authentication. Implement the approved administrator-managed write boundary after review; define administrator provisioning and update fixtures that currently create books as ordinary users. Keep private reading ownership intact.
2. External metadata fields and `MANUAL`/`OPEN_LIBRARY` provider values exist; provider integration, import and refresh do not. Adapter architecture is accepted; implementation policy remains to be designed.
3. Add real MySQL/Flyway verification and investigate test-order/parallel isolation in separately approved work. Do not infer either from H2 success.
4. The service checks duplicate user/book entries, but the database has no corresponding uniqueness constraint; concurrent creation remains a data-integrity risk requiring review.
5. JWT malformed/expired-token response behavior needs verification; the filter does not locally handle parsing exceptions. Session statelessness is not explicitly configured. Neither is claimed as a reproduced failure in this task.
6. Mutable aggregate accessors, default Open EntityManager in View and pagination serialization warnings remain technical debt. Some note unit tests assert repository interaction details, so refactors should preserve behavioral coverage without requiring unnecessary saves.

## Frontend and documentation conflicts

The separate frontend repository's router and home-page source were inspected: routes cover landing, login, registration and a protected home page; home displays account information and logs a book-list response. No reading screen is registered there. This is static source evidence, not runtime or end-to-end verification. The frontend may require adaptation to the changed `/review` route and note collection/nested mutation API; compatibility has not been tested.

The following existing documents/configuration were reviewed and left unchanged:

| Source | Conflict or evidence gap |
| --- | --- |
| [README](README.md) | Calls reviews public, although current resources are owner-scoped; restricts ratings to finished/abandoned, while code only forbids ratings for `TO_READ`; calls authentication stateless without explicit session configuration. React and API versioning appear as roadmap items although a sibling frontend and `/api/v1` routes exist. AWS deployment claims were not independently verified. |
| [SECURITY.md](SECURITY.md) | Lists 1.x as supported and pre-1.0 as unsupported, while the POM is `0.0.1-SNAPSHOT`. Maintainer clarification is needed; private vulnerability reporting remains the stated policy. |
| [backend-v2-design.md](docs/backend-v2-design.md) | Still Draft. Roles, metadata and basic notes have partly overtaken its roadmap; public catalog routes, provider flows, extra note fields, synchronization and community features are not implemented or automatically approved. Its proposed user/book uniqueness constraint is absent from migrations. |
| [.env.example](.env.example) | Uses `JPA_DDL_AUTO=update`, whereas application properties default to `validate`; runtime schema ownership needs configuration review. |

Accepted architecture is recorded in [DECISIONS.md](DECISIONS.md); the nine supplied decisions do not approve every proposal in the older draft. Documentation reconciliation, administrator provisioning, and provider policy remain review topics, not authorization to implement them during NC-DOC-002.
