package com.flashdash.activity.service;

import com.flashdash.activity.exception.ErrorCode;
import com.flashdash.activity.exception.FlashDashException;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.ActivityRepository;
import com.p4r1nc3.flashdash.activity.model.LeaderboardEntry;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LeaderboardServiceTest {

    @MockitoBean
    private ActivityRepository activityRepository;

    @Autowired
    private LeaderboardService leaderboardService;

    private List<Activity> mockActivities;

    @BeforeEach
    void setUp() {
        Activity activity1 = new Activity();
        activity1.setUserFrn("frn:flashdash:user:111111");
        activity1.setActivity(ActivityTypeEnum.GAME_FINISHED);
        activity1.setCreatedAt(LocalDateTime.now());

        Activity activity2 = new Activity();
        activity2.setUserFrn("frn:flashdash:user:111111");
        activity2.setActivity(ActivityTypeEnum.GAME_FINISHED);
        activity2.setCreatedAt(LocalDateTime.now().minusDays(1));

        Activity activity3 = new Activity();
        activity3.setUserFrn("frn:flashdash:user:222222");
        activity3.setActivity(ActivityTypeEnum.GAME_FINISHED);
        activity3.setCreatedAt(LocalDateTime.now().minusDays(1));

        mockActivities = List.of(activity1, activity2, activity3);
    }

    @Test
    void shouldReturnLeaderboardForTotalGamesPlayed() {
        // Arrange
        when(activityRepository.findAll()).thenReturn(mockActivities);
        String criteria = "totalGamesPlayed";

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(criteria, null, 10);

        // Assert
        assertThat(leaderboard).hasSize(2);
        assertThat(leaderboard.get(0).getScore()).isGreaterThanOrEqualTo(leaderboard.get(1).getScore());
        assertThat(leaderboard.get(0).getUserFrn()).isEqualTo("frn:flashdash:user:111111");

        verify(activityRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnLeaderboardForCurrentStreak() {
        // Arrange
        when(activityRepository.findAll()).thenReturn(mockActivities);
        String criteria = "currentStreak";

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(criteria, null, 10);

        // Assert
        assertThat(leaderboard).hasSize(2);
        assertThat(leaderboard.get(0).getScore()).isGreaterThanOrEqualTo(leaderboard.get(1).getScore());
        assertThat(leaderboard.get(0).getUserFrn()).isEqualTo("frn:flashdash:user:111111");

        verify(activityRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnLeaderboardForSpecificUsers() {
        // Arrange
        when(activityRepository.findAll()).thenReturn(mockActivities);
        String criteria = "totalGamesPlayed";
        List<String> includedUsers = List.of("frn:flashdash:user:111111");

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(criteria, includedUsers, 10);

        // Assert
        assertThat(leaderboard).hasSize(1);
        assertThat(leaderboard.get(0).getUserFrn()).isEqualTo("frn:flashdash:user:111111");

        verify(activityRepository, times(1)).findAll();
    }

    @Test
    void shouldReturnLeaderboardWithUsersHavingZeroScore() {
        // Arrange
        when(activityRepository.findAll()).thenReturn(mockActivities);
        String criteria = "totalGamesPlayed";
        List<String> includedUsers = List.of("frn:flashdash:user:333333"); // Użytkownik bez aktywności

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(criteria, includedUsers, 10);

        // Assert
        assertThat(leaderboard).hasSize(1);
        assertThat(leaderboard.get(0).getUserFrn()).isEqualTo("frn:flashdash:user:333333");
        assertThat(leaderboard.get(0).getScore()).isEqualTo(0);

        verify(activityRepository, times(1)).findAll();
    }

    @Test
    void shouldThrowFlashDashExceptionForInvalidCriteria() {
        // Arrange
        when(activityRepository.findAll()).thenReturn(mockActivities);
        String invalidCriteria = "invalidMetric";

        // Act & Assert
        assertThatThrownBy(() -> leaderboardService.getLeaderboard(invalidCriteria, null, 10))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400001)
                .hasMessageContaining("Invalid leaderboard criteria");
    }

    @Test
    void shouldReturnEmptyLeaderboardWhenNoData() {
        // Arrange
        when(activityRepository.findAll()).thenReturn(Collections.emptyList());
        String criteria = "totalGamesPlayed";

        // Act
        List<LeaderboardEntry> leaderboard = leaderboardService.getLeaderboard(criteria, null, 10);

        // Assert
        assertThat(leaderboard).isEmpty();
        verify(activityRepository, times(1)).findAll();
    }
}
