package com.flashdash.activity.controller;

import com.flashdash.activity.FlashDashActivityApplication;
import com.flashdash.activity.service.ActivityStatisticsService;
import com.p4r1nc3.flashdash.activity.model.ActivityStatisticsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = FlashDashActivityApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityStatisticsControllerTest {

    @Autowired
    private ActivityStatisticsController activityStatisticsController;

    @MockitoBean
    private ActivityStatisticsService activityStatisticsService;

    @BeforeEach
    void setUp() {
        activityStatisticsService = mock(ActivityStatisticsService.class);
        activityStatisticsController = new ActivityStatisticsController(activityStatisticsService);
    }

    @Test
    void shouldReturnUserStatisticsSuccessfully() {
        // Arrange
        ActivityStatisticsResponse mockStatistics = new ActivityStatisticsResponse();
        mockStatistics.setCurrentStreak(5);
        mockStatistics.setTotalGamesPlayed(20);
        mockStatistics.setTotalDecksCreated(7);
        mockStatistics.setTotalQuestionsCreated(50);
        mockStatistics.setTotalFriendsAdded(15);

        when(activityStatisticsService.getUserStatistics()).thenReturn(mockStatistics);

        // Act
        ResponseEntity<ActivityStatisticsResponse> response = activityStatisticsController.getUserStatistics();

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCurrentStreak()).isEqualTo(5);
        assertThat(response.getBody().getTotalGamesPlayed()).isEqualTo(20);
        assertThat(response.getBody().getTotalDecksCreated()).isEqualTo(7);
        assertThat(response.getBody().getTotalQuestionsCreated()).isEqualTo(50);
        assertThat(response.getBody().getTotalFriendsAdded()).isEqualTo(15);

        verify(activityStatisticsService, times(1)).getUserStatistics();
    }

    @Test
    void shouldHandleExceptionWhenUserStatisticsFails() {
        // Arrange
        when(activityStatisticsService.getUserStatistics()).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThatThrownBy(() -> activityStatisticsController.getUserStatistics())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unexpected error");

        verify(activityStatisticsService, times(1)).getUserStatistics();
    }
}
