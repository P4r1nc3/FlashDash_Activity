package com.flashdash.activity.controller;

import com.flashdash.activity.FlashDashActivityApplication;
import com.flashdash.activity.service.UserStatisticsService;
import com.p4r1nc3.flashdash.activity.model.UserStatisticsResponse;
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
class UserStatisticsControllerTest {

    @Autowired
    private UserStatisticsController userStatisticsController;

    @MockitoBean
    private UserStatisticsService userStatisticsService;

    @BeforeEach
    void setUp() {
        userStatisticsService = mock(UserStatisticsService.class);
        userStatisticsController = new UserStatisticsController(userStatisticsService);
    }

    @Test
    void shouldReturnUserStatisticsSuccessfully() {
        // Arrange
        UserStatisticsResponse mockStatistics = new UserStatisticsResponse();
        mockStatistics.setCurrentStreak(5);
        mockStatistics.setTotalGamesPlayed(20);
        mockStatistics.setTotalDecksCreated(7);
        mockStatistics.setTotalQuestionsCreated(50);
        mockStatistics.setTotalFriendsAdded(15);

        when(userStatisticsService.getUserStatistics()).thenReturn(mockStatistics);

        // Act
        ResponseEntity<UserStatisticsResponse> response = userStatisticsController.getUserStatistics();

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCurrentStreak()).isEqualTo(5);
        assertThat(response.getBody().getTotalGamesPlayed()).isEqualTo(20);
        assertThat(response.getBody().getTotalDecksCreated()).isEqualTo(7);
        assertThat(response.getBody().getTotalQuestionsCreated()).isEqualTo(50);
        assertThat(response.getBody().getTotalFriendsAdded()).isEqualTo(15);

        verify(userStatisticsService, times(1)).getUserStatistics();
    }

    @Test
    void shouldHandleExceptionWhenUserStatisticsFails() {
        // Arrange
        when(userStatisticsService.getUserStatistics()).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThatThrownBy(() -> userStatisticsController.getUserStatistics())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unexpected error");

        verify(userStatisticsService, times(1)).getUserStatistics();
    }
}
