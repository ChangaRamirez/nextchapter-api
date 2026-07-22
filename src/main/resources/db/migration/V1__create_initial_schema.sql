CREATE TABLE users
(
    id       BINARY(16)   NOT NULL,
    created  DATETIME(6)  NOT NULL,
    email    VARCHAR(255) NOT NULL,
    name     VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    updated  DATETIME(6)  NOT NULL,

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE books
(
    id               BINARY(16)    NOT NULL,
    author           VARCHAR(255)  NOT NULL,
    created          DATETIME(6)   NOT NULL,
    description      VARCHAR(1000) NULL,
    isbn             VARCHAR(17)   NOT NULL,
    publication_year INT           NOT NULL,
    title            VARCHAR(255)  NOT NULL,
    updated          DATETIME(6)   NOT NULL,

    CONSTRAINT pk_books PRIMARY KEY (id),
    CONSTRAINT uk_books_isbn UNIQUE (isbn)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE book_genres
(
    book_id BINARY(16) NOT NULL,
    genre   ENUM (
        'ADVENTURE',
        'BIOGRAPHY',
        'FANTASY',
        'FICTION',
        'HISTORY',
        'HORROR',
        'MYSTERY',
        'PHILOSOPHY',
        'ROMANCE',
        'SCIENCE_FICTION',
        'TECHNOLOGY'
    ) NOT NULL,

    CONSTRAINT pk_book_genres PRIMARY KEY (book_id, genre),
    CONSTRAINT fk_book_genres_book
        FOREIGN KEY (book_id)
            REFERENCES books (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


CREATE TABLE reading_entry
(
    id          BINARY(16)    NOT NULL,
    created     DATETIME(6)   NOT NULL,
    finished_at DATE          NULL,
    notes       VARCHAR(5000) NULL,
    rating      INT           NULL,
    review      VARCHAR(2000) NULL,
    started_at  DATE          NULL,
    status      ENUM (
        'ABANDONED',
        'FINISHED',
        'PAUSED',
        'READING',
        'TO_READ'
    ) NOT NULL,
    updated     DATETIME(6)   NOT NULL,
    book_id     BINARY(16)    NOT NULL,
    user_id     BINARY(16)    NOT NULL,

    CONSTRAINT pk_reading_entry PRIMARY KEY (id),

    INDEX idx_reading_entry_book_id (book_id),
    INDEX idx_reading_entry_user_id (user_id),

    CONSTRAINT fk_reading_entry_book
        FOREIGN KEY (book_id)
            REFERENCES books (id),

    CONSTRAINT fk_reading_entry_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;