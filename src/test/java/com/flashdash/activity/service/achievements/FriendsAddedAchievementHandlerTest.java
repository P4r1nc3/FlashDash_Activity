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
class FriendsAddedAchievementHandlerTest {

    @MockitoBean
    private AchievementRepository achievementRepository;

    @MockitoBean
    private AchievementSchemaRepository schemaRepository;

    @MockitoBean
    private NotificationService notificationService;

    private FriendsAddedAchievementHandler handler;

    @BeforeEach
    void setUp() {
        handler = new FriendsAddedAchievementHandler(achievementRepository, schemaRepository, notificationService);

        // Mock schema repository to return a threshold of 3 for level 1
        AchievementSchema schema = new AchievementSchema(
                "frn:flashdash:achievementschema:friends_added:1",
                AchievementType.FRIENDS_ADDED,
                1,
                3,
                "Social Butterfly Level 1",
                "Add 3 friends"
        );

        Map<Integer, Integer> levelsMap = new TreeMap<>();
        levelsMap.put(1, 3);

        when(schemaRepository.findByAchievementTypeAndLevel(AchievementType.FRIENDS_ADDED, 1))
                .thenReturn(Optional.of(schema));
        when(schemaRepository.findByAchievementType(AchievementType.FRIENDS_ADDED))
                .thenReturn(List.of(schema));

        // Setup notification service mock
        doNothing().when(notificationService).sendAchievementUnlockedEmail();
    }

    @Test
    void shouldGrantFriendsAchievementForLevel1() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = List.of(
                TestUtils.createActivity(userFrn, "frn:friend:001", ActivityTypeEnum.FRIEND_INVITE_RESPONDED),
                TestUtils.createActivity(userFrn, "frn:friend:002", ActivityTypeEnum.FRIEND_INVITE_RESPONDED),
                TestUtils.createActivity(userFrn, "frn:friend:003", ActivityTypeEnum.FRIEND_INVITE_RESPONDED)
        );

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.FRIENDS_ADDED, 1))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert
        ArgumentCaptor<Achievement> captor = ArgumentCaptor.forClass(Achievement.class);
        verify(achievementRepository, times(1)).save(captor.capture());

        Achievement savedAchievement = captor.getValue();
        assertThat(savedAchievement.getUserFrn()).isEqualTo(userFrn);
        assertThat(savedAchievement.getAchievementType()).isEqualTo(AchievementType.FRIENDS_ADDED);
        assertThat(savedAchievement.getLevel()).isEqualTo(1);
        assertThat(savedAchievement.getSchemaFrn()).isEqualTo("frn:flashdash:achievementschema:friends_added:1");

        // Verify that notification service was called once
        verify(notificationService, times(1)).sendAchievementUnlockedEmail();
    }

    @Test
    void shouldNotGrantFriendsAchievementIfAlreadyExists() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = List.of(
                TestUtils.createActivity(userFrn, "frn:friend:001", ActivityTypeEnum.FRIEND_INVITE_RESPONDED),
                TestUtils.createActivity(userFrn, "frn:friend:002", ActivityTypeEnum.FRIEND_INVITE_RESPONDED),
                TestUtils.createActivity(userFrn, "frn:friend:003", ActivityTypeEnum.FRIEND_INVITE_RESPONDED)
        );

        String schemaFrn = "frn:flashdash:achievementschema:friends_added:1";
        Achievement existingAchievement = new Achievement(
                "frn:achievement:1",
                userFrn,
                AchievementType.FRIENDS_ADDED,
                1,
                schemaFrn,
                LocalDateTime.now().minusDays(1)
        );

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.FRIENDS_ADDED, 1))
                .thenReturn(Optional.of(existingAchievement));

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Achievement already exists, so save should not be called
        verify(achievementRepository, never()).save(any(Achievement.class));

        // Verify notification service was not called
        verify(notificationService, never()).sendAchievementUnlockedEmail();
    }

    @Test
    void shouldNotGrantFriendsAchievementIfThresholdNotMet() {
        // Arrange
        String userFrn = "frn:user:" + UUID.randomUUID();
        List<Activity> activities = List.of(
                TestUtils.createActivity(userFrn, "frn:friend:001", ActivityTypeEnum.FRIEND_INVITE_RESPONDED),
                TestUtils.createActivity(userFrn, "frn:friend:002", ActivityTypeEnum.FRIEND_INVITE_RESPONDED)
        ); // Only 2 friends, threshold is 3

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.FRIENDS_ADDED, 1))
                .thenReturn(Optional.empty());

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Threshold not met, so save should not be called
        verify(achievementRepository, never()).save(any(Achievement.class));

        // Verify notification service was not called
        verify(notificationService, never()).sendAchievementUnlockedEmail();
    }
}