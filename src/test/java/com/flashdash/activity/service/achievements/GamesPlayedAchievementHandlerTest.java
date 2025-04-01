package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.AchievementSchemaRepository;
import com.flashdash.activity.service.api.NotificationService;
import com.flashdash.activity.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GamesPlayedAchievementHandlerTest {

    @MockitoBean
    private AchievementRepository achievementRepository;

    @MockitoBean
    private AchievementSchemaRepository schemaRepository;

    @MockitoBean
    private NotificationService notificationService;

    private GamesPlayedAchievementHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GamesPlayedAchievementHandler(achievementRepository, schemaRepository, notificationService);

        // Mock schemas for levels 1-2
        List<AchievementSchema> schemas = new ArrayList<>();

        // Level 1: 10 games
        schemas.add(new AchievementSchema(
                "frn:flashdash:achievementschema:games_played:1",
                AchievementType.GAMES_PLAYED,
                1,
                10,
                "Games Played Level 1",
                "Play 10 games"
        ));

        // Level 2: 20 games
        schemas.add(new AchievementSchema(
                "frn:flashdash:achievementschema:games_played:2",
                AchievementType.GAMES_PLAYED,
                2,
                20,
                "Games Played Level 2",
                "Play 20 games"
        ));

        // Mock schema repository
        when(schemaRepository.findByAchievementType(AchievementType.GAMES_PLAYED))
                .thenReturn(schemas);

        // Mock findByAchievementTypeAndLevel for each level
        for (AchievementSchema schema : schemas) {
            when(schemaRepository.findByAchievementTypeAndLevel(AchievementType.GAMES_PLAYED, schema.getLevel()))
                    .thenReturn(Optional.of(schema));
        }

        // Make sure notification service doesn't throw any exceptions
        doNothing().when(notificationService).sendAchievementUnlockedEmail();
    }

    @Test
    void shouldGrantGamesPlayedAchievementForLevel1() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createGameActivities(userFrn, 10);

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.GAMES_PLAYED, 1))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert
        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository, times(1)).save(captor.capture());

        Achievement savedAchievement = captor.getValue();
        assertThat(savedAchievement.getUserFrn()).isEqualTo(userFrn);
        assertThat(savedAchievement.getAchievementType()).isEqualTo(AchievementType.GAMES_PLAYED);
        assertThat(savedAchievement.getLevel()).isEqualTo(1);
        assertThat(savedAchievement.getSchemaFrn()).isEqualTo("frn:flashdash:achievementschema:games_played:1");

        verify(notificationService, times(1)).sendAchievementUnlockedEmail();
    }

    @Test
    void shouldNotGrantGamesPlayedAchievementIfAlreadyExists() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createGameActivities(userFrn, 10);

        String schemaFrn = "frn:flashdash:achievementschema:games_played:1";
        Achievement existingAchievement = new Achievement(
                "frn:achievement:1",
                userFrn,
                AchievementType.GAMES_PLAYED,
                1,
                schemaFrn,
                LocalDateTime.now().minusDays(1)
        );

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.GAMES_PLAYED, 1))
                .thenReturn(Optional.of(existingAchievement));

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Achievement already exists, so save should not be called for level 1
        verify(achievementRepository, never()).save(any(Achievement.class));

        // Verify notification was not sent
        verify(notificationService, never()).sendAchievementUnlockedEmail();
    }

    @Test
    void shouldGrantMultipleGamesPlayedAchievementsIfThresholdsMet() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createGameActivities(userFrn, 100); // 100 games played

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.GAMES_PLAYED, 1))
                .thenReturn(Optional.empty());
        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.GAMES_PLAYED, 2))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Levels 1 and 2 should be granted (updated from 3 to 2 levels)
        verify(achievementRepository, times(2)).save(any(Achievement.class));

        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository, times(2)).save(captor.capture());

        List<Achievement> savedAchievements = captor.getAllValues();
        assertThat(savedAchievements).hasSize(2);

        assertThat(savedAchievements).extracting(Achievement::getLevel)
                .containsExactly(1, 2);

        // Verify notification was sent twice (once for each achievement)
        verify(notificationService, times(2)).sendAchievementUnlockedEmail();
    }

    @Test
    void shouldGrantOnlyHigherLevelsIfLowerLevelsAlreadyExist() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createGameActivities(userFrn, 100); // 100 games played

        // Level 1 already exists
        Achievement existingLevel1 = new Achievement(
                "frn:achievement:1",
                userFrn,
                AchievementType.GAMES_PLAYED,
                1,
                "frn:flashdash:achievementschema:games_played:1",
                LocalDateTime.now().minusDays(1)
        );

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.GAMES_PLAYED, 1))
                .thenReturn(Optional.of(existingLevel1));
        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.GAMES_PLAYED, 2))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Only level 2 should be granted (level 1 already exists)
        // Updated expectation from 2 to 1 achievement
        verify(achievementRepository, times(1)).save(any(Achievement.class));

        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository, times(1)).save(captor.capture());

        List<Achievement> savedAchievements = captor.getAllValues();
        assertThat(savedAchievements).hasSize(1);

        assertThat(savedAchievements).extracting(Achievement::getLevel)
                .containsExactly(2);

        // Verify notification was sent once (for the one new achievement)
        verify(notificationService, times(1)).sendAchievementUnlockedEmail();
    }
}