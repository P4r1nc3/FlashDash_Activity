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
class QuestionsCreatedAchievementHandlerTest {

    @MockitoBean
    private AchievementRepository achievementRepository;

    @Autowired
    private QuestionsCreatedAchievementHandler handler;

    @BeforeEach
    void setUp() {
        handler = new QuestionsCreatedAchievementHandler(achievementRepository);
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
    }

    @Test
    void shouldNotGrantQuestionAchievementIfAlreadyExists() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = TestUtils.createQuestionActivities(userFrn, 10);

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.QUESTIONS_CREATED, 1))
                .thenReturn(Optional.of(new Achievement("frn:achievement:1", userFrn, AchievementType.QUESTIONS_CREATED, 1, null)));

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert
        verify(achievementRepository, never()).save(any(Achievement.class));
    }
}
