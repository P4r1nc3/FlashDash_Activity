package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.AchievementSchemaRepository;
import com.flashdash.activity.service.api.NotificationService;
import com.flashdash.activity.util.TestUtils;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StreakAchievementHandlerTest {

    @MockitoBean
    private AchievementRepository achievementRepository;

    @MockitoBean
    private AchievementSchemaRepository schemaRepository;

    @MockitoBean
    private NotificationService notificationService;

    private StreakAchievementHandler handler;

    @BeforeEach
    void setUp() {
        handler = new StreakAchievementHandler(achievementRepository, schemaRepository, notificationService);

        // Mock schemas for levels 1-4
        List<AchievementSchema> schemas = new ArrayList<>();

        // Level 1: 3 day streak
        schemas.add(new AchievementSchema(
                "frn:flashdash:achievementschema:streak_achievement:1",
                AchievementType.STREAK_ACHIEVEMENT,
                1,
                3,
                "Streak Achievement Level 1",
                "Achieve a 3-day streak"
        ));

        // Level 2: 7 day streak
        schemas.add(new AchievementSchema(
                "frn:flashdash:achievementschema:streak_achievement:2",
                AchievementType.STREAK_ACHIEVEMENT,
                2,
                7,
                "Streak Achievement Level 2",
                "Achieve a 7-day streak"
        ));

        // Level 3: 14 day streak
        schemas.add(new AchievementSchema(
                "frn:flashdash:achievementschema:streak_achievement:3",
                AchievementType.STREAK_ACHIEVEMENT,
                3,
                14,
                "Streak Achievement Level 3",
                "Achieve a 14-day streak"
        ));

        // Level 4: 30 day streak
        schemas.add(new AchievementSchema(
                "frn:flashdash:achievementschema:streak_achievement:4",
                AchievementType.STREAK_ACHIEVEMENT,
                4,
                30,
                "Streak Achievement Level 4",
                "Achieve a 30-day streak"
        ));

        // Mock schema repository
        when(schemaRepository.findByAchievementType(AchievementType.STREAK_ACHIEVEMENT))
                .thenReturn(schemas);

        // Mock findByAchievementTypeAndLevel for each level
        for (AchievementSchema schema : schemas) {
            when(schemaRepository.findByAchievementTypeAndLevel(AchievementType.STREAK_ACHIEVEMENT, schema.getLevel()))
                    .thenReturn(Optional.of(schema));
        }

        // Setup notification service mock
        doNothing().when(notificationService).sendAchievementUnlockedEmail();
    }

    @Test
    void shouldGrantStreakAchievementForLevel1() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createConsecutiveGameStreak(userFrn, 3);

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.STREAK_ACHIEVEMENT, 1))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert
        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository, times(1)).save(captor.capture());

        Achievement savedAchievement = captor.getValue();
        assertThat(savedAchievement.getUserFrn()).isEqualTo(userFrn);
        assertThat(savedAchievement.getAchievementType()).isEqualTo(AchievementType.STREAK_ACHIEVEMENT);
        assertThat(savedAchievement.getLevel()).isEqualTo(1);
        assertThat(savedAchievement.getSchemaFrn()).isEqualTo("frn:flashdash:achievementschema:streak_achievement:1");

        // Verify notification was sent once
        verify(notificationService, times(1)).sendAchievementUnlockedEmail();
    }

    @Test
    void shouldNotGrantStreakAchievementIfAlreadyExists() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createConsecutiveGameStreak(userFrn, 3);

        String schemaFrn = "frn:flashdash:achievementschema:streak_achievement:1";
        Achievement existingAchievement = new Achievement(
                "frn:achievement:1",
                userFrn,
                AchievementType.STREAK_ACHIEVEMENT,
                1,
                schemaFrn,
                LocalDateTime.now().minusDays(1)
        );

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.STREAK_ACHIEVEMENT, 1))
                .thenReturn(Optional.of(existingAchievement));

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert
        verify(achievementRepository, never()).save(any(Achievement.class));

        // Verify notification was not sent
        verify(notificationService, never()).sendAchievementUnlockedEmail();
    }

    @Test
    void shouldGrantHigherStreakAchievementLevels() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createConsecutiveGameStreak(userFrn, 30);

        for (int level = 1; level <= 4; level++) {
            when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.STREAK_ACHIEVEMENT, level))
                    .thenReturn(Optional.empty());
        }

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert
        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository, times(4)).save(captor.capture());

        List<Achievement> savedAchievements = captor.getAllValues();
        assertThat(savedAchievements).hasSize(4);

        assertThat(savedAchievements)
                .extracting(Achievement::getLevel)
                .containsExactly(1, 2, 3, 4);

        // Verify schema FRNs are correct
        assertThat(savedAchievements)
                .extracting(Achievement::getSchemaFrn)
                .containsExactly(
                        "frn:flashdash:achievementschema:streak_achievement:1",
                        "frn:flashdash:achievementschema:streak_achievement:2",
                        "frn:flashdash:achievementschema:streak_achievement:3",
                        "frn:flashdash:achievementschema:streak_achievement:4"
                );

        // Verify notification was sent once for each achievement level
        verify(notificationService, times(4)).sendAchievementUnlockedEmail();
    }

    @Test
    void shouldNotGrantAchievementForInsufficientStreak() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createConsecutiveGameStreak(userFrn, 2); // Only 2 days, threshold is 3

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.STREAK_ACHIEVEMENT, 1))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Threshold not met, so save should not be called
        verify(achievementRepository, never()).save(any(Achievement.class));

        // Verify notification was not sent
        verify(notificationService, never()).sendAchievementUnlockedEmail();
    }
}