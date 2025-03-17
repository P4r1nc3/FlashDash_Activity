package com.flashdash.activity.controller;

import com.flashdash.activity.exception.ErrorCode;
import com.flashdash.activity.exception.FlashDashException;
import com.flashdash.activity.service.LeaderboardService;
import com.p4r1nc3.flashdash.activity.model.LeaderboardEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LeaderboardControllerTest {

    @Autowired
    private LeaderboardController leaderboardController;

    @MockitoBean
    private LeaderboardService leaderboardService;

    private String userFrn1;
    private String userFrn2;

    @BeforeEach
    void setUp() {
        userFrn1 = "frn:flashdash:user:" + UUID.randomUUID();
        userFrn2 = "frn:flashdash:user:" + UUID.randomUUID();
    }

    @Test
    void shouldGetLeaderboardSuccessfully() {
        // Arrange
        LeaderboardEntry entry1 = new LeaderboardEntry();
        entry1.setRank(1);
        entry1.setUserFrn(userFrn1);
        entry1.setScore(50);

        LeaderboardEntry entry2 = new LeaderboardEntry();
        entry2.setRank(2);
        entry2.setUserFrn(userFrn2);
        entry2.setScore(40);

        List<LeaderboardEntry> mockLeaderboard = List.of(entry1, entry2);
        String criteria = "totalGamesPlayed";
        int limit = 10;

        when(leaderboardService.getLeaderboard(criteria, null, limit)).thenReturn(mockLeaderboard);

        // Act
        ResponseEntity<List<LeaderboardEntry>> response = leaderboardController.getLeaderboards(criteria, null, limit);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull().hasSize(2);
        assertThat(response.getBody().get(0).getUserFrn()).isEqualTo(userFrn1);
        assertThat(response.getBody().get(0).getScore()).isEqualTo(50);
        assertThat(response.getBody().get(1).getUserFrn()).isEqualTo(userFrn2);
        assertThat(response.getBody().get(1).getScore()).isEqualTo(40);

        verify(leaderboardService, times(1)).getLeaderboard(criteria, null, limit);
    }

    @Test
    void shouldReturnEmptyListWhenNoLeaderboardEntriesFound() {
        // Arrange
        String criteria = "totalDecksCreated";
        int limit = 5;
        when(leaderboardService.getLeaderboard(criteria, null, limit)).thenReturn(List.of());

        // Act
        ResponseEntity<List<LeaderboardEntry>> response = leaderboardController.getLeaderboards(criteria, null, limit);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();

        verify(leaderboardService, times(1)).getLeaderboard(criteria, null, limit);
    }

    @Test
    void shouldThrowFlashDashExceptionWhenInvalidCriteria() {
        // Arrange
        String invalidCriteria = "invalidCriteria";
        int limit = 10;
        doThrow(new FlashDashException(ErrorCode.E400001, "Invalid leaderboard criteria: " + invalidCriteria))
                .when(leaderboardService).getLeaderboard(invalidCriteria, null, limit);

        // Act & Assert
        assertThatThrownBy(() -> leaderboardController.getLeaderboards(invalidCriteria, null, limit))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400001)
                .hasMessage("Invalid leaderboard criteria: " + invalidCriteria);

        verify(leaderboardService, times(1)).getLeaderboard(invalidCriteria, null, limit);
    }

    @Test
    void shouldGetLeaderboardForSpecificUsers() {
        // Arrange
        LeaderboardEntry entry1 = new LeaderboardEntry();
        entry1.setRank(1);
        entry1.setUserFrn(userFrn1);
        entry1.setScore(50);

        List<String> includedUsers = List.of(userFrn1, userFrn2);
        String criteria = "currentStreak";
        int limit = 5;

        when(leaderboardService.getLeaderboard(criteria, includedUsers, limit)).thenReturn(List.of(entry1));

        // Act
        ResponseEntity<List<LeaderboardEntry>> response = leaderboardController.getLeaderboards(criteria, includedUsers, limit);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        assertThat(response.getBody().get(0).getUserFrn()).isEqualTo(userFrn1);
        assertThat(response.getBody().get(0).getScore()).isEqualTo(50);

        verify(leaderboardService, times(1)).getLeaderboard(criteria, includedUsers, limit);
    }
}
