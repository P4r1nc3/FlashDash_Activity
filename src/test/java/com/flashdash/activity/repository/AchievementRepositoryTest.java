package com.flashdash.activity.repository;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AchievementRepositoryTest {

    @Autowired
    private AchievementRepository achievementRepository;

    @BeforeEach
    void setUp() {
        achievementRepository.deleteAll();
    }

    // Helper method to create test achievements with all required fields
    private Achievement createTestAchievement() {
        String userFrn = "frn:user:" + UUID.randomUUID();
        AchievementType type = AchievementType.GAMES_PLAYED;
        int level = 1;
        String achievementFrn = "frn:flashdash:achievement:" + UUID.randomUUID();
        String schemaFrn = "frn:flashdash:achievementschema:" + type.name().toLowerCase() + ":" + level;

        Achievement achievement = new Achievement();
        achievement.setAchievementFrn(achievementFrn);
        achievement.setUserFrn(userFrn);
        achievement.setAchievementType(type);
        achievement.setLevel(level);
        achievement.setSchemaFrn(schemaFrn); // Set the schemaFrn that was missing before
        achievement.setAchievedAt(LocalDateTime.now());

        return achievement;
    }

    @Test
    void shouldFindAchievementsByUserFrn() {
        // Arrange
        Achievement achievement1 = createTestAchievement();
        Achievement achievement2 = createTestAchievement();
        achievement2.setUserFrn(achievement1.getUserFrn());
        achievementRepository.save(achievement1);
        achievementRepository.save(achievement2);

        // Act
        List<Achievement> achievements = achievementRepository.findByUserFrn(achievement1.getUserFrn());

        // Assert
        assertThat(achievements).hasSize(2);
        assertThat(achievements).extracting(Achievement::getUserFrn).containsOnly(achievement1.getUserFrn());
    }

    @Test
    void shouldReturnEmptyListWhenNoAchievementsForUserFrn() {
        // Act
        List<Achievement> achievements = achievementRepository.findByUserFrn("frn:user:nonexistent");

        // Assert
        assertThat(achievements).isEmpty();
    }

    @Test
    void shouldFindSpecificAchievementLevel() {
        // Arrange
        Achievement achievement = createTestAchievement();
        achievementRepository.save(achievement);

        // Act
        Optional<Achievement> foundAchievement = achievementRepository.findByUserFrnAndAchievementTypeAndLevel(
                achievement.getUserFrn(), achievement.getAchievementType(), achievement.getLevel());

        // Assert
        assertThat(foundAchievement).isPresent();
        assertThat(foundAchievement.get().getUserFrn()).isEqualTo(achievement.getUserFrn());
        assertThat(foundAchievement.get().getAchievementType()).isEqualTo(achievement.getAchievementType());
        assertThat(foundAchievement.get().getLevel()).isEqualTo(achievement.getLevel());
    }

    @Test
    void shouldReturnEmptyOptionalWhenAchievementLevelNotFound() {
        // Act
        Optional<Achievement> foundAchievement = achievementRepository.findByUserFrnAndAchievementTypeAndLevel(
                "frn:user:unknown", AchievementType.GAMES_PLAYED, 99);

        // Assert
        assertThat(foundAchievement).isEmpty();
    }

    @Test
    void shouldFindAchievementByFrnAndUserFrn() {
        // Arrange
        Achievement achievement = createTestAchievement();
        achievementRepository.save(achievement);

        // Act
        Optional<Achievement> foundAchievement = achievementRepository.findByAchievementFrnAndUserFrn(
                achievement.getAchievementFrn(), achievement.getUserFrn());

        // Assert
        assertThat(foundAchievement).isPresent();
        assertThat(foundAchievement.get().getAchievementFrn()).isEqualTo(achievement.getAchievementFrn());
        assertThat(foundAchievement.get().getUserFrn()).isEqualTo(achievement.getUserFrn());
    }

    @Test
    void shouldReturnEmptyOptionalWhenAchievementFrnNotFound() {
        // Act
        Optional<Achievement> foundAchievement = achievementRepository.findByAchievementFrnAndUserFrn(
                "frn:flashdash:achievement:nonexistent", "frn:user:123");

        // Assert
        assertThat(foundAchievement).isEmpty();
    }

    @Test
    void shouldFindAchievementsByUserFrnOrderedByAchievedAtDesc() {
        // Arrange
        Achievement oldestAchievement = createTestAchievement();
        oldestAchievement.setAchievedAt(LocalDateTime.now().minusDays(5));

        Achievement middleAchievement = createTestAchievement();
        middleAchievement.setUserFrn(oldestAchievement.getUserFrn());
        middleAchievement.setAchievedAt(LocalDateTime.now().minusDays(3));

        Achievement newestAchievement = createTestAchievement();
        newestAchievement.setUserFrn(oldestAchievement.getUserFrn());
        newestAchievement.setAchievedAt(LocalDateTime.now().minusDays(1));

        achievementRepository.save(oldestAchievement);
        achievementRepository.save(middleAchievement);
        achievementRepository.save(newestAchievement);

        // Act
        List<Achievement> achievements = achievementRepository.findByUserFrnOrderByAchievedAtDesc(oldestAchievement.getUserFrn());

        // Assert
        assertThat(achievements).hasSize(3);
        assertThat(achievements.get(0).getAchievedAt()).isAfterOrEqualTo(achievements.get(1).getAchievedAt());
        assertThat(achievements.get(1).getAchievedAt()).isAfterOrEqualTo(achievements.get(2).getAchievedAt());
    }

    @Test
    void shouldReturnEmptyListWhenNoAchievementsForUserFrnOrderedByAchievedAt() {
        // Act
        List<Achievement> achievements = achievementRepository.findByUserFrnOrderByAchievedAtDesc("frn:user:nonexistent");

        // Assert
        assertThat(achievements).isEmpty();
    }
}