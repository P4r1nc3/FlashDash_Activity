package com.flashdash.activity.service;

import com.flashdash.activity.exception.ErrorCode;
import com.flashdash.activity.exception.FlashDashException;
import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.util.TestUtils;
import com.flashdash.activity.util.UserContext;
import com.p4r1nc3.flashdash.activity.model.AchievementResponse;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AchievementServiceTest {

    @Autowired
    private AchievementService achievementService;

    @MockitoBean
    private UserContext userContext;

    @MockitoBean
    private AchievementRepository achievementRepository;

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

    @Test
    void shouldGetUserAchievementsSuccessfully() {
        // Arrange
        String userFrn = "frn:user:123";
        when(userContext.getUserFrn()).thenReturn(userFrn);

        List<Achievement> mockAchievements = List.of(
                TestUtils.createAchievement(userFrn, AchievementType.GAMES_PLAYED, 2),
                TestUtils.createAchievement(userFrn, AchievementType.STREAK_ACHIEVEMENT, 1)
        );

        when(achievementRepository.findByUserFrn(userFrn)).thenReturn(mockAchievements);

        // Act
        List<AchievementResponse> achievementResponses = achievementService.getUserAchievements();

        // Assert
        assertThat(achievementResponses).hasSize(2);
        assertThat(achievementResponses.stream()
                .map(AchievementResponse::getAchievementType)
                .collect(Collectors.toList()))
                .containsExactlyInAnyOrder(AchievementResponse.AchievementTypeEnum.GAMES_PLAYED, AchievementResponse.AchievementTypeEnum.STREAK_ACHIEVEMENT);

        verify(achievementRepository, times(1)).findByUserFrn(userFrn);
    }

    @Test
    void shouldThrow401WhenUserIsNotAuthenticated() {
        // Arrange
        when(userContext.getUserFrn()).thenReturn(null);

        // Act & Assert
        FlashDashException exception = catchThrowableOfType(
                () -> achievementService.getUserAchievements(),
                FlashDashException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.E401001);
        assertThat(exception.getMessage()).isEqualTo("User is not authenticated");

        verifyNoInteractions(achievementRepository);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoAchievements() {
        // Arrange
        String userFrn = "frn:user:empty";
        when(userContext.getUserFrn()).thenReturn(userFrn);
        when(achievementRepository.findByUserFrn(userFrn)).thenReturn(List.of());

        // Act
        List<AchievementResponse> achievementResponses = achievementService.getUserAchievements();

        // Assert
        assertThat(achievementResponses).isEmpty();
        verify(achievementRepository, times(1)).findByUserFrn(userFrn);
    }

    @Test
    void shouldGetAchievementByFrnSuccessfully() {
        // Arrange
        String userFrn = "frn:user:123";
        String achievementFrn = "frn:flashdash:achievement:5";

        when(userContext.getUserFrn()).thenReturn(userFrn);

        Achievement mockAchievement = TestUtils.createAchievement(userFrn, AchievementType.GAMES_PLAYED, 2);
        mockAchievement.setAchievementFrn(achievementFrn);

        when(achievementRepository.findByAchievementFrnAndUserFrn(achievementFrn, userFrn))
                .thenReturn(Optional.of(mockAchievement));

        // Act
        AchievementResponse achievementResponse = achievementService.getAchievementByFrn(achievementFrn);

        // Assert
        assertThat(achievementResponse).isNotNull();
        assertThat(achievementResponse.getAchievementFrn()).isEqualTo(achievementFrn);
        assertThat(achievementResponse.getAchievementType()).isEqualTo(AchievementResponse.AchievementTypeEnum.GAMES_PLAYED);

        verify(achievementRepository, times(1)).findByAchievementFrnAndUserFrn(achievementFrn, userFrn);
    }


    @Test
    void shouldThrow404WhenAchievementNotFound() {
        // Arrange
        String userFrn = "frn:user:123";
        String nonExistentFrn = "frn:flashdash:achievement:nonexistent";

        when(userContext.getUserFrn()).thenReturn(userFrn);
        when(achievementRepository.findByAchievementFrnAndUserFrn(nonExistentFrn, userFrn))
                .thenReturn(Optional.empty());

        // Act & Assert
        FlashDashException exception = catchThrowableOfType(
                () -> achievementService.getAchievementByFrn(nonExistentFrn),
                FlashDashException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.E404001);
        assertThat(exception.getMessage()).isEqualTo("Achievement not found");

        verify(achievementRepository, times(1)).findByAchievementFrnAndUserFrn(nonExistentFrn, userFrn);
    }
}
