package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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

    @Autowired
    private StreakAchievementHandler handler;

    @BeforeEach
    void setUp() {
        handler = new StreakAchievementHandler(achievementRepository);
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
    }

    @Test
    void shouldNotGrantStreakAchievementIfAlreadyExists() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createConsecutiveGameStreak(userFrn, 3);

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.STREAK_ACHIEVEMENT, 1))
                .thenReturn(Optional.of(new Achievement("frn:achievement:1", userFrn, AchievementType.STREAK_ACHIEVEMENT, 1, null)));

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert
        verify(achievementRepository, never()).save(any(Achievement.class));
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
                .containsExactlyInAnyOrder(1, 2, 3, 4);
    }

}
