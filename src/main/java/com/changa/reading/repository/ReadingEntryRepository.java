package com.changa.reading.repository;

import com.changa.reading.domain.entity.ReadingEntry;
import com.changa.reading.domain.entity.ReadingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ReadingEntryRepository extends JpaRepository<ReadingEntry, UUID> {

    Page<ReadingEntry> findByUser_Id(UUID userId, Pageable pageable);

    Optional<ReadingEntry> findByIdAndUser_Id(UUID readingEntryId, UUID userId);

    Page<ReadingEntry> findByCreatedGreaterThanEqualAndUser_Id(Instant createdAfter, UUID userId, Pageable pageable);

    boolean existsByUser_IdAndBook_Id(UUID userId, UUID bookId);

    long countByStatusAndUser_Id(ReadingStatus status, UUID userId);

    long countByUser_Id(UUID userId);

    @Query("""
            SELECT AVG(r.rating)
            FROM ReadingEntry r
            WHERE r.rating IS NOT NULL
            AND r.user.id = :userId
            """)
    Double findAverageRating(UUID userId);
}
