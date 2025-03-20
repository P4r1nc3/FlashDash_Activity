package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.AchievementSchemaRepository;
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

    private GamesPlayedAchievementHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GamesPlayedAchievementHandler(achievementRepository, schemaRepository);

        // Mock schemas for levels 1-3
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

        // Level 2: 50 games
        schemas.add(new AchievementSchema(
                "frn:flashdash:achievementschema:games_played:2",
                AchievementType.GAMES_PLAYED,
                2,
                50,
                "Games Played Level 2",
                "Play 50 games"
        ));

        // Level 3: 100 games
        schemas.add(new AchievementSchema(
                "frn:flashdash:achievementschema:games_played:3",
                AchievementType.GAMES_PLAYED,
                3,
                100,
                "Games Played Level 3",
                "Play 100 games"
        ));

        // Mock schema repository
        when(schemaRepository.findByAchievementType(AchievementType.GAMES_PLAYED))
                .thenReturn(schemas);

        // Mock findByAchievementTypeAndLevel for each level
        for (AchievementSchema schema : schemas) {
            when(schemaRepository.findByAchievementTypeAndLevel(AchievementType.GAMES_PLAYED, schema.getLevel()))
                    .thenReturn(Optional.of(schema));
        }
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
        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.GAMES_PLAYED, 3))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Levels 1, 2, and 3 should be granted
        verify(achievementRepository, times(3)).save(any(Achievement.class));

        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository, times(3)).save(captor.capture());

        List<Achievement> savedAchievements = captor.getAllValues();
        assertThat(savedAchievements).hasSize(3);

        assertThat(savedAchievements).extracting(Achievement::getLevel)
                .containsExactly(1, 2, 3);
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
        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.GAMES_PLAYED, 3))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Only levels 2 and 3 should be granted (level 1 already exists)
        verify(achievementRepository, times(2)).save(any(Achievement.class));

        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository, times(2)).save(captor.capture());

        List<Achievement> savedAchievements = captor.getAllValues();
        assertThat(savedAchievements).hasSize(2);

        assertThat(savedAchievements).extracting(Achievement::getLevel)
                .containsExactly(2, 3);
    }
}