# NextChapter Backend v2 Design

> **Status:** Draft
> **Version:** 2.0
> **Last Updated:** July 2026

---

# Vision

NextChapter is **not** a book catalog application.

NextChapter is a **personal reading management platform** where users can discover books, organize their library, track their reading journey, and build a personal archive of reviews and notes.

Books are the medium.

The user's relationship with those books is the domain.

---

# Design Principles

## 1. User-Centered Domain

The application's primary responsibility is managing a user's reading experience.

Everything should answer the question:

> "How does this improve the user's personal reading journey?"

---

## 2. External Catalog

Books should not be manually curated inside the application.

Instead, NextChapter integrates with external book providers to discover and import metadata.

Examples:

- Open Library
- Google Books
- Future providers

The backend owns the integration layer while keeping the frontend provider-agnostic.

---

## 3. Local Metadata Snapshot

When a user interacts with a book, the backend stores a normalized local snapshot.

This guarantees:

- Referential integrity
- Fast access
- Stable statistics
- Independence from external API changes
- Future provider migration

---

## 4. Clean Domain Separation

External providers own book metadata.

NextChapter owns:

- users
- authentication
- reading entries
- reviews
- notes
- reading statistics
- authorization

---

# Domain Model

## User

Represents an authenticated account.

Responsibilities:

- Authentication
- Authorization
- Personal library ownership

### Planned additions

- Role

---

## Book

Represents normalized metadata imported from an external provider.

A Book **does not** represent a user's interaction.

### Responsibilities

- Metadata
- Provider reference
- External identifiers

### Planned fields

```java
UUID id;

BookProvider provider;

String externalId;

String title;

String description;

String isbn;

String author;

Integer publicationYear;

Set<BookGenre> genres;

String coverUrl;

Instant metadataFetchedAt;

Instant created;

Instant updated;
```

### Notes

Books should become mostly immutable after creation.

Metadata updates should happen through synchronization rather than user edits.

---

## ReadingEntry

Represents a user's relationship with a specific book.

This is the heart of the application.

### Responsibilities

- Reading status
- Rating
- Review
- Reading dates
- Personal ownership

### Planned fields

```java
UUID id;

User user;

Book book;

ReadingStatus status;

Integer rating;

String review;

LocalDate startedAt;

LocalDate finishedAt;

Instant created;

Instant updated;
```

### Notes

A user can only own one ReadingEntry for a given Book.

Unique constraint:

(User, Book)

---

## ReadingNote

Represents an individual journal entry during the reading process.

Instead of storing one long block of text, users can create multiple notes over time.

Example:

Day 1

> Gandalf seems suspicious.

Day 5

> The Mines of Moria were incredible.

Day 10

> I absolutely loved the ending.

### Planned fields

```java
UUID id;

ReadingEntry readingEntry;

String title;

String content;

Integer pageNumber;

String chapter;

boolean spoiler;

Instant created;

Instant updated;
```

Future possibilities:

- Search notes
- Timeline view
- Export journal
- Reading history

---

# Authorization

## Roles

### USER

Can:

- Browse catalog
- Search books
- Manage personal ReadingEntries
- Manage ReadingNotes
- Manage Reviews

Cannot:

- Modify catalog
- Delete books
- Edit global metadata

---

### ADMIN

Can additionally:

- Create books manually
- Edit imported metadata
- Delete books
- Refresh metadata
- Moderate future community content

---

# Catalog Architecture

```
React Frontend
        │
        ▼
Spring Boot API
        │
        ▼
BookCatalogProvider
        │
 ┌──────┴────────┐
 │               │
 ▼               ▼
Open Library   Google Books
```

The frontend should never depend directly on an external provider.

---

# Public vs Protected Endpoints

## Public

```
POST /auth/register

POST /auth/login

GET /catalog/search

GET /catalog/books/{id}

GET /books/{id}
```

---

## Authenticated

```
/reading-entries/**

/notes/**

/users/me/**
```

---

## Admin

```
POST /books

PUT /books/{id}

DELETE /books/{id}
```

---

# Book Import Flow

```
User searches book
        │
        ▼
Backend queries provider
        │
        ▼
User selects book
        │
        ▼
Backend checks local database
        │
        ├── Exists
        │       │
        │       ▼
        │   Reuse Book
        │
        └── Doesn't exist
                │
                ▼
        Import metadata
                │
                ▼
           Create Book
                │
                ▼
      Create ReadingEntry
```

---

# Product Philosophy

A ReadingEntry represents:

> "My relationship with this book."

A ReadingNote represents:

> "A thought I had while reading."

A Review represents:

> "My overall opinion of the book."

These are intentionally different concepts.

---

# Deferred Features

The following are intentionally postponed.

## Authors as entities

Authors remain Strings.

Reason:

Managing author relationships introduces unnecessary complexity for the product.

---

## Community Features

Deferred:

- Public profiles
- Following users
- Likes
- Public reviews
- Comments

The current focus is building an excellent personal reading experience.

---

## Reading Goals

Examples:

- Books per year
- Pages per month
- Reading streaks

Planned after Backend v2.

---

## Recommendations

Possible future additions:

- Similar books
- Personalized suggestions
- Trending books

Not part of Backend v2.

---

# Backend v2 Roadmap

## Phase 1

- Flyway
- Database migrations
- Domain redesign

---

## Phase 2

- Role-based authorization

---

## Phase 3

- External catalog integration

---

## Phase 4

- Local book synchronization

---

## Phase 5

- Reading Notes

---

## Phase 6

- Public catalog endpoints

---

## Phase 7

- Frontend integration

---

# Definition of Backend v2 Complete

Backend v2 is considered complete when:

- Authentication is production-ready.
- Authorization supports USER and ADMIN roles.
- Books are imported from an external provider.
- Local metadata snapshots are maintained.
- ReadingEntry represents the user's reading journey.
- ReadingNote supports multiple notes per book.
- Public catalog browsing is available.
- Swagger documentation is updated.
- Database migrations are versioned with Flyway.
- AWS deployment reflects the new architecture.
- Existing tests are updated and new functionality is covered by automated tests.

---

# Guiding Principle

> **Books belong to the world's catalog.**
>
> **Reading belongs to the user.**
>
> NextChapter exists to preserve and enrich that personal reading journey.