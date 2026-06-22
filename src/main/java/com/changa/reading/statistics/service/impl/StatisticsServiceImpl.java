package com.changa.reading.statistics.service.impl;

import com.changa.auth.service.AuthenticatedUserService;
import com.changa.reading.domain.entity.ReadingStatus;
import com.changa.reading.repository.ReadingEntryRepository;
import com.changa.reading.statistics.domain.dto.StatisticsDto;
import com.changa.reading.statistics.service.StatisticsService;
import com.changa.user.domain.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final ReadingEntryRepository readingEntryRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public StatisticsServiceImpl(ReadingEntryRepository readingEntryRepository, AuthenticatedUserService authenticatedUserService) {
        this.readingEntryRepository = readingEntryRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Override
    public StatisticsDto getStatistics() {

        UUID userId = authenticatedUserService.getCurrentUser().getId();

        return new StatisticsDto(
                readingEntryRepository.countByUser_Id(userId),
                readingEntryRepository.countByStatusAndUser_Id(ReadingStatus.FINISHED, userId),
                readingEntryRepository.countByStatusAndUser_Id(ReadingStatus.READING, userId),
                readingEntryRepository.countByStatusAndUser_Id(ReadingStatus.TO_READ, userId),
                readingEntryRepository.findAverageRating(userId)
        );
    }
    
}
