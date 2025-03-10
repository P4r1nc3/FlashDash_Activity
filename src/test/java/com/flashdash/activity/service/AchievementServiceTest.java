package com.flashdash.activity.service;

import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.util.TestUtils;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AchievementServiceTest {

    @Autowired
    private AchievementService achievementService;

    @MockitoBean
    private ActivityRepository activityRepository;

    @Test
    void shouldCheckAchievementsForUserSuccessfully() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> mockActivities = List.of(
                TestUtils.createActivity(userFrn, "frn:game:123", ActivityTypeEnum.GAME_FINISHED),
                TestUtils.createActivity(userFrn, "frn:deck:456", ActivityTypeEnum.DECK_CREATED)
        );

        when(activityRepository.findByUserFrn(userFrn)).thenReturn(mockActivities);

        // Act
        achievementService.checkAchievementsForUser(userFrn);

        // Assert
        verify(activityRepository, times(1)).findByUserFrn(userFrn);
    }

    @Test
    void shouldHandleEmptyActivityList() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        when(activityRepository.findByUserFrn(userFrn)).thenReturn(List.of());

        // Act
        achievementService.checkAchievementsForUser(userFrn);

        // Assert
        verify(activityRepository, times(1)).findByUserFrn(userFrn);
    }

    @Test
    void shouldBeAsynchronous() throws NoSuchMethodException {
        boolean isAsync = AchievementService.class.getMethod("checkAchievementsForUser", String.class)
                .isAnnotationPresent(Async.class);

        assertThat(isAsync).isTrue();
    }
}
