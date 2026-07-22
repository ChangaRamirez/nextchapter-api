ALTER TABLE books
    MODIFY COLUMN isbn VARCHAR(17) NULL,
    ADD COLUMN provider VARCHAR(50) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN external_id VARCHAR(255) NULL,
    ADD COLUMN cover_url VARCHAR(2048) NULL,
    ADD COLUMN metadata_fetched_at TIMESTAMP NULL,
    ADD CONSTRAINT uk_books_provider_external_id
    UNIQUE (provider, external_id);