package com.changa.reading.statistics.service.impl;

import com.changa.auth.service.AuthenticatedUserService;
import com.changa.reading.domain.entity.ReadingStatus;
import com.changa.reading.repository.ReadingEntryRepository;
import com.changa.reading.statistics.domain.dto.StatisticsDto;
import com.changa.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.changa.support.TestDataFactory.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    ReadingEntryRepository readingEntryRepository;

    @Mock
    AuthenticatedUserService authenticatedUserService;

    @InjectMocks
    StatisticsServiceImpl statisticsService;

    @Test
    void getStatistics_shouldReturnCurrentUserStatistics() {

        User currentUser = createUser();

        when(authenticatedUserService.getCurrentUser()).thenReturn(currentUser);
        when(readingEntryRepository.countByUser_Id(currentUser.getId())).thenReturn(10L);
        when(readingEntryRepository.countByStatusAndUser_Id(ReadingStatus.FINISHED, currentUser.getId())).thenReturn(4L);
        when(readingEntryRepository.countByStatusAndUser_Id(ReadingStatus.READING, currentUser.getId())).thenReturn(2L);
        when(readingEntryRepository.countByStatusAndUser_Id(ReadingStatus.TO_READ, currentUser.getId())).thenReturn(4L);
        when(readingEntryRepository.findAverageRating(currentUser.getId())).thenReturn(8.5);

        StatisticsDto response = statisticsService.getStatistics();

        assertThat(response.totalEntries()).isEqualTo(10L);
        assertThat(response.booksFinished()).isEqualTo(4L);
        assertThat(response.booksReading()).isEqualTo(2L);
        assertThat(response.booksToRead()).isEqualTo(4L);
        assertThat(response.averageRating()).isEqualTo(8.5);

        verify(readingEntryRepository).countByUser_Id(currentUser.getId());
        verify(readingEntryRepository).countByStatusAndUser_Id(ReadingStatus.FINISHED, currentUser.getId());
        verify(readingEntryRepository).countByStatusAndUser_Id(ReadingStatus.READING, currentUser.getId());
        verify(readingEntryRepository).countByStatusAndUser_Id(ReadingStatus.TO_READ, currentUser.getId());
        verify(readingEntryRepository).findAverageRating(currentUser.getId());
    }

}