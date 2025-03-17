package com.flashdash.activity.service;

import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.util.UserContext;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.p4r1nc3.flashdash.activity.model.ActivityStatisticsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityStatisticsServiceTest {

    @Autowired
    private ActivityStatisticsService activityStatisticsService;

    @MockitoBean
    private ActivityRepository activityRepository;

    @MockitoBean
    private UserContext userContext;

    private String userFrn;

    @BeforeEach
    void setUp() {
        activityRepository = mock(ActivityRepository.class);
        userContext = mock(UserContext.class);
        activityStatisticsService = new ActivityStatisticsService(activityRepository, userContext);

        userFrn = "frn:flashdash:user:" + UUID.randomUUID();
        when(userContext.getUserFrn()).thenReturn(userFrn);
    }

    @Test
    void shouldReturnUserStatisticsSuccessfully() {
        // Arrange
        List<Activity> activities = List.of(
                createActivity(ActivityTypeEnum.GAME_FINISHED, 0),
                createActivity(ActivityTypeEnum.GAME_FINISHED, -1),
                createActivity(ActivityTypeEnum.GAME_FINISHED, -2),
                createActivity(ActivityTypeEnum.DECK_CREATED, 0),
                createActivity(ActivityTypeEnum.QUESTION_CREATED, 0),
                createActivity(ActivityTypeEnum.QUESTION_CREATED, 0),
                createActivity(ActivityTypeEnum.QUESTION_CREATED, 0),
                createActivity(ActivityTypeEnum.FRIEND_INVITE_RESPONDED, 0)
        );

        when(activityRepository.findByUserFrn(userFrn)).thenReturn(activities);

        // Act
        ActivityStatisticsResponse statistics = activityStatisticsService.getUserStatistics();

        // Assert
        assertThat(statistics).isNotNull();
        assertThat(statistics.getCurrentStreak()).isEqualTo(3);
        assertThat(statistics.getTotalGamesPlayed()).isEqualTo(3);
        assertThat(statistics.getTotalDecksCreated()).isEqualTo(1);
        assertThat(statistics.getTotalQuestionsCreated()).isEqualTo(3);
        assertThat(statistics.getTotalFriendsAdded()).isEqualTo(1);

        verify(activityRepository, times(1)).findByUserFrn(userFrn);
    }

    @Test
    void shouldReturnZeroStatisticsWhenNoActivitiesExist() {
        // Arrange
        when(activityRepository.findByUserFrn(userFrn)).thenReturn(List.of());

        // Act
        ActivityStatisticsResponse statistics = activityStatisticsService.getUserStatistics();

        // Assert
        assertThat(statistics).isNotNull();
        assertThat(statistics.getCurrentStreak()).isEqualTo(0);
        assertThat(statistics.getTotalGamesPlayed()).isEqualTo(0);
        assertThat(statistics.getTotalDecksCreated()).isEqualTo(0);
        assertThat(statistics.getTotalQuestionsCreated()).isEqualTo(0);
        assertThat(statistics.getTotalFriendsAdded()).isEqualTo(0);

        verify(activityRepository, times(1)).findByUserFrn(userFrn);
    }

    @Test
    void shouldCalculateStreakCorrectlyWhenDaysAreNotConsecutive() {
        // Arrange
        List<Activity> activities = List.of(
                createActivity(ActivityTypeEnum.GAME_FINISHED, 0),
                createActivity(ActivityTypeEnum.GAME_FINISHED, -2),
                createActivity(ActivityTypeEnum.GAME_FINISHED, -5)
        );

        when(activityRepository.findByUserFrn(userFrn)).thenReturn(activities);

        // Act
        ActivityStatisticsResponse statistics = activityStatisticsService.getUserStatistics();

        // Assert
        assertThat(statistics.getCurrentStreak()).isEqualTo(1); // Reset when not consecutive
    }

    @Test
    void shouldCalculateStreakWhenUserHasMultipleActivitiesPerDay() {
        // Arrange
        List<Activity> activities = List.of(
                createActivity(ActivityTypeEnum.GAME_FINISHED, 0),
                createActivity(ActivityTypeEnum.GAME_FINISHED, 0),
                createActivity(ActivityTypeEnum.GAME_FINISHED, -1),
                createActivity(ActivityTypeEnum.GAME_FINISHED, -1),
                createActivity(ActivityTypeEnum.GAME_FINISHED, -2)
        );

        when(activityRepository.findByUserFrn(userFrn)).thenReturn(activities);

        // Act
        ActivityStatisticsResponse statistics = activityStatisticsService.getUserStatistics();

        // Assert
        assertThat(statistics.getCurrentStreak()).isEqualTo(3);
    }

    private Activity createActivity(ActivityTypeEnum type, int daysAgo) {
        Activity activity = new Activity();
        activity.setUserFrn(userFrn);
        activity.setTargetFrn("frn:flashdash:deck:" + UUID.randomUUID());
        activity.setActivity(type);
        activity.setCreatedAt(LocalDateTime.now().minusDays(daysAgo));
        activity.setUpdatedAt(LocalDateTime.now().minusDays(daysAgo));
        return activity;
    }
}
