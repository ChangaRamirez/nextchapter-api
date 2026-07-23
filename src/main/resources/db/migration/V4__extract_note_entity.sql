CREATE TABLE reading_note
(
    id               BINARY(16)   NOT NULL,
    content          VARCHAR(5000) NOT NULL,
    reading_entry_id BINARY(16)   NOT NULL,
    created          DATETIME(6)  NOT NULL,
    updated          DATETIME(6)  NOT NULL,

    CONSTRAINT pk_reading_note
        PRIMARY KEY (id),

    CONSTRAINT fk_reading_note_reading_entry
        FOREIGN KEY (reading_entry_id)
            REFERENCES reading_entry (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;


INSERT INTO reading_note (
    id,
    content,
    reading_entry_id,
    created,
    updated
)
SELECT
    UUID_TO_BIN(UUID()),
    notes,
    id,
    created,
    updated
FROM reading_entry
WHERE notes IS NOT NULL
  AND TRIM(notes) <> '';


ALTER TABLE reading_entry
DROP COLUMN notes;