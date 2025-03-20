package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.AchievementSchemaRepository;
import com.flashdash.activity.util.TestUtils;
import java.time.LocalDateTime;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DecksCreatedAchievementHandlerTest {

    @MockitoBean
    private AchievementRepository achievementRepository;

    @MockitoBean
    private AchievementSchemaRepository schemaRepository;

    private DecksCreatedAchievementHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DecksCreatedAchievementHandler(achievementRepository, schemaRepository);

        // Mock schema repository to return a threshold of 3 for level 1
        AchievementSchema schema = new AchievementSchema(
                "frn:flashdash:achievementschema:decks_created:1",
                AchievementType.DECKS_CREATED,
                1,
                3,
                "Deck Creator Level 1",
                "Create 3 decks"
        );

        Map<Integer, Integer> levelsMap = new TreeMap<>();
        levelsMap.put(1, 3);

        when(schemaRepository.findByAchievementTypeAndLevel(AchievementType.DECKS_CREATED, 1))
                .thenReturn(Optional.of(schema));
        when(schemaRepository.findByAchievementType(AchievementType.DECKS_CREATED))
                .thenReturn(List.of(schema));
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
        assertThat(savedAchievement.getSchemaFrn()).isEqualTo("frn:flashdash:achievementschema:decks_created:1");
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

        String schemaFrn = "frn:flashdash:achievementschema:decks_created:1";
        Achievement existingAchievement = new Achievement(
                "frn:achievement:1",
                userFrn,
                AchievementType.DECKS_CREATED,
                1,
                schemaFrn,
                LocalDateTime.now().minusDays(1)
        );

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.DECKS_CREATED, 1))
                .thenReturn(Optional.of(existingAchievement));

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Achievement already exists, so save should not be called
        verify(achievementRepository, never()).save(any(Achievement.class));
    }

    @Test
    void shouldNotGrantDeckAchievementIfThresholdNotMet() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = List.of(
                TestUtils.createActivity(userFrn, "frn:deck:123", ActivityTypeEnum.DECK_CREATED),
                TestUtils.createActivity(userFrn, "frn:deck:124", ActivityTypeEnum.DECK_CREATED)
        ); // Only 2 decks, threshold is 3

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.DECKS_CREATED, 1))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Threshold not met, so save should not be called
        verify(achievementRepository, never()).save(any(Achievement.class));
    }
}