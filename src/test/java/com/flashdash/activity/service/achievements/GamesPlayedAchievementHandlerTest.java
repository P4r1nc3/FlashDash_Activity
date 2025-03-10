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
class GamesPlayedAchievementHandlerTest {

    @MockitoBean
    private AchievementRepository achievementRepository;

    @Autowired
    private GamesPlayedAchievementHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GamesPlayedAchievementHandler(achievementRepository);
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
    }

    @Test
    void shouldNotGrantGamesPlayedAchievementIfAlreadyExists() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createGameActivities(userFrn, 10); // 10 rozegranych gier

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.GAMES_PLAYED, 1))
                .thenReturn(Optional.of(new Achievement("frn:achievement:1", userFrn, AchievementType.GAMES_PLAYED, 1, null)));

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Osiągnięcie już istnieje, więc `save` nie powinno być wywołane
        verify(achievementRepository, never()).save(any(Achievement.class));
    }

    @Test
    void shouldGrantMultipleGamesPlayedAchievementsIfThresholdsMet() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createGameActivities(userFrn, 100); // 100 rozegranych gier

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.GAMES_PLAYED, 1))
                .thenReturn(Optional.empty());
        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.GAMES_PLAYED, 2))
                .thenReturn(Optional.empty());
        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.GAMES_PLAYED, 3))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - powinny zostać przyznane poziomy 1, 2 i 3
        verify(achievementRepository, times(3)).save(any(Achievement.class));
    }
}
