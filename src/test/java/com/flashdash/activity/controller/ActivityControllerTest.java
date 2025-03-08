package com.flashdash.activity.controller;

import com.flashdash.activity.model.Activity;
import com.flashdash.activity.service.ActivityService;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.flashdash.activity.exception.FlashDashException;
import com.flashdash.activity.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityControllerTest {

    private ActivityController activityController;

    @MockitoBean
    private ActivityService activityService;

    private String userFrn;
    private String targetFrn;

    @BeforeEach
    void setUp() {
        activityService = mock(ActivityService.class);
        activityController = new ActivityController(activityService);
        userFrn = "frn:flashdash:user:" + UUID.randomUUID();
        targetFrn = "frn:flashdash:deck:" + UUID.randomUUID();
    }

    @Test
    void shouldLogActivitySuccessfully() {
        // Arrange
        LogActivityRequest request = new LogActivityRequest();
        request.setTargetFrn(targetFrn);
        request.setActivityType(ActivityTypeEnum.GAME_STARTED);

        Activity mockActivity = new Activity();
        mockActivity.setUserFrn(userFrn);
        mockActivity.setTargetFrn(targetFrn);
        mockActivity.setActivity(ActivityTypeEnum.GAME_STARTED);

        when(activityService.logActivity(any())).thenReturn(mockActivity);

        // Act
        ResponseEntity<Void> response = activityController.logActivity(request);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        verify(activityService, times(1)).logActivity(request);
    }

    @Test
    void shouldThrowFlashDashExceptionWhenLogActivityFails() {
        // Arrange
        LogActivityRequest request = new LogActivityRequest();
        request.setTargetFrn(targetFrn);
        request.setActivityType(LogActivityRequest.ActivityTypeEnum.GAME_STARTED);

        doThrow(new FlashDashException(ErrorCode.E401001, "Token is missing or invalid"))
                .when(activityService).logActivity(request);

        // Act & Assert
        assertThatThrownBy(() -> activityController.logActivity(request))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E401001)
                .hasMessage("Token is missing or invalid");

        verify(activityService, times(1)).logActivity(request);
    }

    @Test
    void shouldGetUserActivitiesSuccessfully() {
        // Arrange
        Activity activity = new Activity();
        activity.setUserFrn(userFrn);
        activity.setTargetFrn(targetFrn);
        activity.setActivity(LogActivityRequest.ActivityTypeEnum.GAME_STARTED);

        when(activityService.getUserActivities()).thenReturn(List.of(activity));

        // Act
        ResponseEntity<List<Activity>> response = activityController.getUserActivities();

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull().hasSize(1);
        assertThat(response.getBody().get(0).getUserFrn()).isEqualTo(userFrn);
        assertThat(response.getBody().get(0).getTargetFrn()).isEqualTo(targetFrn);
        assertThat(response.getBody().get(0).getActivity()).isEqualTo(ActivityTypeEnum.GAME_STARTED);

        verify(activityService, times(1)).getUserActivities();
    }

    @Test
    void shouldReturnEmptyListWhenNoUserActivitiesFound() {
        // Arrange
        when(activityService.getUserActivities()).thenReturn(List.of());

        // Act
        ResponseEntity<List<Activity>> response = activityController.getUserActivities();

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();

        verify(activityService, times(1)).getUserActivities();
    }

    @Test
    void shouldThrowFlashDashExceptionWhenGetUserActivitiesFails() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E401001, "Token is missing or invalid"))
                .when(activityService).getUserActivities();

        // Act & Assert
        assertThatThrownBy(() -> activityController.getUserActivities())
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E401001)
                .hasMessage("Token is missing or invalid");

        verify(activityService, times(1)).getUserActivities();
    }
}
