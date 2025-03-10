package com.flashdash.activity.repository;

import com.flashdash.activity.FlashDashActivityApplication;
import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(classes = FlashDashActivityApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AchievementRepositoryTest {

    @Autowired
    private AchievementRepository achievementRepository;

    @BeforeEach
    void setUp() {
        achievementRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveAchievement() {
        // Arrange
        Achievement achievement = TestUtils.createAchievement();
        achievementRepository.save(achievement);

        // Act
        List<Achievement> foundAchievements = achievementRepository.findByUserFrn(achievement.getUserFrn());

        // Assert
        assertThat(foundAchievements).hasSize(1);
        Achievement savedAchievement = foundAchievements.get(0);
        assertThat(savedAchievement.getUserFrn()).isEqualTo(achievement.getUserFrn());
        assertThat(savedAchievement.getAchievementType()).isEqualTo(achievement.getAchievementType());
        assertThat(savedAchievement.getLevel()).isEqualTo(achievement.getLevel());
    }

    @Test
    void shouldFindAchievementsByUserFrn() {
        // Arrange
        Achievement achievement1 = TestUtils.createAchievement();
        Achievement achievement2 = TestUtils.createAchievement();
        achievementRepository.save(achievement1);
        achievementRepository.save(achievement2);

        // Act
        List<Achievement> achievements = achievementRepository.findByUserFrn(achievement1.getUserFrn());

        // Assert
        assertThat(achievements).isNotEmpty();
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
        Achievement achievement = TestUtils.createAchievement();
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
}
