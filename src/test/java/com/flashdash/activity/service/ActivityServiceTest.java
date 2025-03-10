package com.flashdash.activity.service;

import com.flashdash.activity.FlashDashActivityApplication;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.util.UserContext;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.flashdash.activity.exception.FlashDashException;
import com.flashdash.activity.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityServiceTest {

    @Autowired
    private ActivityService activityService;

    @MockitoBean
    private UserContext userContext;

    @MockitoBean
    private AchievementService achievementService;

    @MockitoBean
    private ActivityRepository activityRepository;

    private String userFrn;
    private String targetFrn;

    @BeforeEach
    void setUp() {
        userFrn = "frn:flashdash:user:" + UUID.randomUUID();
        targetFrn = "frn:flashdash:deck:" + UUID.randomUUID();
    }

    @Test
    void shouldLogActivitySuccessfully() {
        // Arrange
        LogActivityRequest request = new LogActivityRequest();
        request.setTargetFrn(targetFrn);
        request.setActivityType(LogActivityRequest.ActivityTypeEnum.GAME_STARTED);

        when(userContext.getUserFrn()).thenReturn(userFrn);
        ArgumentCaptor<Activity> captor = ArgumentCaptor.forClass(Activity.class);

        // Act
        activityService.logActivity(request);

        // Assert
        verify(activityRepository).save(captor.capture());

        Activity savedActivity = captor.getValue();
        assertThat(savedActivity.getUserFrn()).isEqualTo(userFrn);
        assertThat(savedActivity.getTargetFrn()).isEqualTo(targetFrn);
        assertThat(savedActivity.getActivity()).isEqualTo(LogActivityRequest.ActivityTypeEnum.GAME_STARTED);
        assertThat(savedActivity.getActivityFrn()).startsWith("frn:flashdash:activity:");
        assertThat(savedActivity.getCreatedAt()).isNotNull();
        assertThat(savedActivity.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldThrowFlashDashExceptionWhenUserNotAuthenticated() {
        // Arrange
        when(userContext.getUserFrn()).thenReturn(null);
        LogActivityRequest request = new LogActivityRequest();
        request.setTargetFrn(targetFrn);
        request.setActivityType(LogActivityRequest.ActivityTypeEnum.GAME_STARTED);

        // Act & Assert
        assertThatThrownBy(() -> activityService.logActivity(request))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E401001)
                .hasMessage("User is not authenticated");

        verifyNoInteractions(activityRepository);
    }

    @Test
    void shouldReturnUserActivitiesSuccessfully() {
        // Arrange
        Activity activity = new Activity();
        activity.setUserFrn(userFrn);
        activity.setTargetFrn(targetFrn);
        activity.setActivity(LogActivityRequest.ActivityTypeEnum.GAME_STARTED);

        when(userContext.getUserFrn()).thenReturn(userFrn);
        when(activityRepository.findByUserFrn(userFrn)).thenReturn(List.of(activity));

        // Act
        List<Activity> activities = activityService.getUserActivities();

        // Assert
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getUserFrn()).isEqualTo(userFrn);
        assertThat(activities.get(0).getTargetFrn()).isEqualTo(targetFrn);
        assertThat(activities.get(0).getActivity()).isEqualTo(ActivityTypeEnum.GAME_STARTED);

        verify(activityRepository, times(1)).findByUserFrn(userFrn);
    }

    @Test
    void shouldReturnEmptyListWhenNoUserActivitiesFound() {
        // Arrange
        when(userContext.getUserFrn()).thenReturn(userFrn);
        when(activityRepository.findByUserFrn(userFrn)).thenReturn(List.of());

        // Act
        List<Activity> activities = activityService.getUserActivities();

        // Assert
        assertThat(activities).isEmpty();
        verify(activityRepository, times(1)).findByUserFrn(userFrn);
    }
}
