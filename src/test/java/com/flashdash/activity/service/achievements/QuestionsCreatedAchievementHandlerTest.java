package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.AchievementSchemaRepository;
import com.flashdash.activity.util.TestUtils;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionsCreatedAchievementHandlerTest {

    @MockitoBean
    private AchievementRepository achievementRepository;

    @MockitoBean
    private AchievementSchemaRepository schemaRepository;

    private QuestionsCreatedAchievementHandler handler;

    @BeforeEach
    void setUp() {
        handler = new QuestionsCreatedAchievementHandler(achievementRepository, schemaRepository);

        // Mock schema repository to return a threshold of 10 for level 1
        AchievementSchema schema = new AchievementSchema(
                "frn:flashdash:achievementschema:questions_created:1",
                AchievementType.QUESTIONS_CREATED,
                1,
                10,
                "Question Creator Level 1",
                "Create 10 questions"
        );

        when(schemaRepository.findByAchievementTypeAndLevel(AchievementType.QUESTIONS_CREATED, 1))
                .thenReturn(Optional.of(schema));
        when(schemaRepository.findByAchievementType(AchievementType.QUESTIONS_CREATED))
                .thenReturn(List.of(schema));
    }

    @Test
    void shouldGrantQuestionAchievementForLevel1() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createQuestionActivities(userFrn, 10);

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.QUESTIONS_CREATED, 1))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert
        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository, times(1)).save(captor.capture());

        Achievement savedAchievement = captor.getValue();
        assertThat(savedAchievement.getUserFrn()).isEqualTo(userFrn);
        assertThat(savedAchievement.getAchievementType()).isEqualTo(AchievementType.QUESTIONS_CREATED);
        assertThat(savedAchievement.getLevel()).isEqualTo(1);
        assertThat(savedAchievement.getSchemaFrn()).isEqualTo("frn:flashdash:achievementschema:questions_created:1");
    }

    @Test
    void shouldNotGrantQuestionAchievementIfAlreadyExists() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createQuestionActivities(userFrn, 10);

        String schemaFrn = "frn:flashdash:achievementschema:questions_created:1";
        Achievement existingAchievement = new Achievement(
                "frn:achievement:1",
                userFrn,
                AchievementType.QUESTIONS_CREATED,
                1,
                schemaFrn,
                LocalDateTime.now().minusDays(1)
        );

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.QUESTIONS_CREATED, 1))
                .thenReturn(Optional.of(existingAchievement));

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert
        verify(achievementRepository, never()).save(any(Achievement.class));
    }

    @Test
    void shouldNotGrantQuestionAchievementIfThresholdNotMet() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createQuestionActivities(userFrn, 9); // Only 9 questions, threshold is 10

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.QUESTIONS_CREATED, 1))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Threshold not met, so save should not be called
        verify(achievementRepository, never()).save(any(Achievement.class));
    }
}