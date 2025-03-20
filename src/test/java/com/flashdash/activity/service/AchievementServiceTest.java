package com.flashdash.activity.service;

import com.flashdash.activity.exception.ErrorCode;
import com.flashdash.activity.exception.FlashDashException;
import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.AchievementSchemaRepository;
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
    private AchievementSchemaRepository schemaRepository;

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

        // Create mock achievements
        Achievement achievement1 = TestUtils.createAchievement(userFrn, AchievementType.GAMES_PLAYED, 2);
        achievement1.setSchemaFrn("frn:flashdash:achievementschema:games_played:2");

        Achievement achievement2 = TestUtils.createAchievement(userFrn, AchievementType.STREAK_ACHIEVEMENT, 1);
        achievement2.setSchemaFrn("frn:flashdash:achievementschema:streak_achievement:1");

        List<Achievement> mockAchievements = List.of(achievement1, achievement2);

        // Create mock schemas
        AchievementSchema schema1 = new AchievementSchema(
                "frn:flashdash:achievementschema:games_played:2",
                AchievementType.GAMES_PLAYED,
                2,
                50,
                "Games Played Level 2",
                "Play 50 games"
        );

        AchievementSchema schema2 = new AchievementSchema(
                "frn:flashdash:achievementschema:streak_achievement:1",
                AchievementType.STREAK_ACHIEVEMENT,
                1,
                3,
                "Streak Level 1",
                "Play for 3 days in a row"
        );

        List<String> schemaFrns = List.of(
                "frn:flashdash:achievementschema:games_played:2",
                "frn:flashdash:achievementschema:streak_achievement:1"
        );

        when(achievementRepository.findByUserFrnOrderByAchievedAtDesc(userFrn)).thenReturn(mockAchievements);
        when(schemaRepository.findAllById(schemaFrns)).thenReturn(List.of(schema1, schema2));

        // Act
        List<AchievementResponse> achievementResponses = achievementService.getUserAchievements();

        // Assert
        assertThat(achievementResponses).hasSize(2);
        assertThat(achievementResponses.stream()
                .map(AchievementResponse::getAchievementType)
                .collect(Collectors.toList()))
                .containsExactlyInAnyOrder(AchievementResponse.AchievementTypeEnum.GAMES_PLAYED, AchievementResponse.AchievementTypeEnum.STREAK_ACHIEVEMENT);

        verify(achievementRepository, times(1)).findByUserFrnOrderByAchievedAtDesc(userFrn);
        verify(schemaRepository, times(1)).findAllById(anyList());
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
        verifyNoInteractions(schemaRepository);
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoAchievements() {
        // Arrange
        String userFrn = "frn:user:empty";
        when(userContext.getUserFrn()).thenReturn(userFrn);
        when(achievementRepository.findByUserFrnOrderByAchievedAtDesc(userFrn)).thenReturn(List.of());

        // Act
        List<AchievementResponse> achievementResponses = achievementService.getUserAchievements();

        // Assert
        assertThat(achievementResponses).isEmpty();
        verify(achievementRepository, times(1)).findByUserFrnOrderByAchievedAtDesc(userFrn);
        verifyNoInteractions(schemaRepository);
    }

    @Test
    void shouldGetAchievementByFrnSuccessfully() {
        // Arrange
        String userFrn = "frn:user:123";
        String achievementFrn = "frn:flashdash:achievement:5";
        String schemaFrn = "frn:flashdash:achievementschema:games_played:2";

        when(userContext.getUserFrn()).thenReturn(userFrn);

        Achievement mockAchievement = TestUtils.createAchievement(userFrn, AchievementType.GAMES_PLAYED, 2);
        mockAchievement.setAchievementFrn(achievementFrn);
        mockAchievement.setSchemaFrn(schemaFrn);

        AchievementSchema mockSchema = new AchievementSchema(
                schemaFrn,
                AchievementType.GAMES_PLAYED,
                2,
                50,
                "Games Played Level 2",
                "Play 50 games"
        );

        when(achievementRepository.findByAchievementFrnAndUserFrn(achievementFrn, userFrn))
                .thenReturn(Optional.of(mockAchievement));
        when(schemaRepository.findBySchemaFrn(schemaFrn))
                .thenReturn(Optional.of(mockSchema));

        // Act
        AchievementResponse achievementResponse = achievementService.getAchievementByFrn(achievementFrn);

        // Assert
        assertThat(achievementResponse).isNotNull();
        assertThat(achievementResponse.getAchievementFrn()).isEqualTo(achievementFrn);
        assertThat(achievementResponse.getAchievementType()).isEqualTo(AchievementResponse.AchievementTypeEnum.GAMES_PLAYED);
        assertThat(achievementResponse.getLevel()).isEqualTo(2);
        assertThat(achievementResponse.getName()).isEqualTo("Games Played Level 2");
        assertThat(achievementResponse.getDescription()).isEqualTo("Play 50 games");
        assertThat(achievementResponse.getThreshold()).isEqualTo(50);

        verify(achievementRepository, times(1)).findByAchievementFrnAndUserFrn(achievementFrn, userFrn);
        verify(schemaRepository, times(1)).findBySchemaFrn(schemaFrn);
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
        verifyNoInteractions(schemaRepository);
    }

    @Test
    void shouldThrow404WhenSchemaNotFound() {
        // Arrange
        String userFrn = "frn:user:123";
        String achievementFrn = "frn:flashdash:achievement:5";
        String schemaFrn = "frn:flashdash:achievementschema:games_played:2";

        when(userContext.getUserFrn()).thenReturn(userFrn);

        Achievement mockAchievement = TestUtils.createAchievement(userFrn, AchievementType.GAMES_PLAYED, 2);
        mockAchievement.setAchievementFrn(achievementFrn);
        mockAchievement.setSchemaFrn(schemaFrn);

        when(achievementRepository.findByAchievementFrnAndUserFrn(achievementFrn, userFrn))
                .thenReturn(Optional.of(mockAchievement));
        when(schemaRepository.findBySchemaFrn(schemaFrn))
                .thenReturn(Optional.empty());

        // Act & Assert
        FlashDashException exception = catchThrowableOfType(
                () -> achievementService.getAchievementByFrn(achievementFrn),
                FlashDashException.class
        );

        assertThat(exception).isNotNull();
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.E404001);
        assertThat(exception.getMessage()).isEqualTo("Achievement schema not found");

        verify(achievementRepository, times(1)).findByAchievementFrnAndUserFrn(achievementFrn, userFrn);
        verify(schemaRepository, times(1)).findBySchemaFrn(schemaFrn);
    }

    @Test
    void shouldGetAllAchievementSchemasSuccessfully() {
        // Arrange
        List<AchievementSchema> mockSchemas = List.of(
                new AchievementSchema(
                        "frn:flashdash:achievementschema:games_played:1",
                        AchievementType.GAMES_PLAYED,
                        1,
                        10,
                        "Games Played Level 1",
                        "Play 10 games"
                ),
                new AchievementSchema(
                        "frn:flashdash:achievementschema:streak_achievement:1",
                        AchievementType.STREAK_ACHIEVEMENT,
                        1,
                        3,
                        "Streak Level 1",
                        "Play for 3 days in a row"
                )
        );

        when(schemaRepository.findAll()).thenReturn(mockSchemas);

        // Act
        List<AchievementResponse> responses = achievementService.getAllAchievementSchemas();

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.stream()
                .map(AchievementResponse::getAchievementType)
                .collect(Collectors.toList()))
                .containsExactlyInAnyOrder(AchievementResponse.AchievementTypeEnum.GAMES_PLAYED, AchievementResponse.AchievementTypeEnum.STREAK_ACHIEVEMENT);

        verify(schemaRepository, times(1)).findAll();
    }

    @Test
    void shouldGetAchievementSchemasByTypeSuccessfully() {
        // Arrange
        AchievementType type = AchievementType.GAMES_PLAYED;

        List<AchievementSchema> mockSchemas = List.of(
                new AchievementSchema(
                        "frn:flashdash:achievementschema:games_played:1",
                        AchievementType.GAMES_PLAYED,
                        1,
                        10,
                        "Games Played Level 1",
                        "Play 10 games"
                ),
                new AchievementSchema(
                        "frn:flashdash:achievementschema:games_played:2",
                        AchievementType.GAMES_PLAYED,
                        2,
                        50,
                        "Games Played Level 2",
                        "Play 50 games"
                )
        );

        when(schemaRepository.findByAchievementTypeOrderByLevelAsc(type)).thenReturn(mockSchemas);

        // Act
        List<AchievementResponse> responses = achievementService.getAchievementSchemasByType(type);

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.stream()
                .map(AchievementResponse::getLevel)
                .collect(Collectors.toList()))
                .containsExactly(1, 2);

        verify(schemaRepository, times(1)).findByAchievementTypeOrderByLevelAsc(type);
    }
}