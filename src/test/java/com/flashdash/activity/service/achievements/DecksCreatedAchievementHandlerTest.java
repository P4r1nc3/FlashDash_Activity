package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.util.TestUtils;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
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
class DecksCreatedAchievementHandlerTest {

    @MockitoBean
    private AchievementRepository achievementRepository;

    @Autowired
    private DecksCreatedAchievementHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DecksCreatedAchievementHandler(achievementRepository);
    }

    @Test
    void shouldGrantDeckAchievementForLevel1() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = List.of(
                TestUtils.createActivity(userFrn, "frn:deck:123", ActivityTypeEnum.DECK_CREATED),
                TestUtils.createActivity(userFrn, "frn:deck:124", ActivityTypeEnum.DECK_CREATED),
                TestUtils.createActivity(userFrn, "frn:deck:125", ActivityTypeEnum.DECK_CREATED)
        );

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.DECKS_CREATED, 1))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert
        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository, times(1)).save(captor.capture());

        Achievement savedAchievement = captor.getValue();
        assertThat(savedAchievement.getUserFrn()).isEqualTo(userFrn);
        assertThat(savedAchievement.getAchievementType()).isEqualTo(AchievementType.DECKS_CREATED);
        assertThat(savedAchievement.getLevel()).isEqualTo(1);
    }

    @Test
    void shouldNotGrantDeckAchievementIfAlreadyExists() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = List.of(
                TestUtils.createActivity(userFrn, "frn:deck:123", ActivityTypeEnum.DECK_CREATED),
                TestUtils.createActivity(userFrn, "frn:deck:124", ActivityTypeEnum.DECK_CREATED),
                TestUtils.createActivity(userFrn, "frn:deck:125", ActivityTypeEnum.DECK_CREATED)
        );

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.DECKS_CREATED, 1))
                .thenReturn(Optional.of(new Achievement("frn:achievement:1", userFrn, AchievementType.DECKS_CREATED, 1, null)));

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Osiągnięcie już istnieje, więc `save` nie powinno być wywołane
        verify(achievementRepository, never()).save(any(Achievement.class));
    }
}
