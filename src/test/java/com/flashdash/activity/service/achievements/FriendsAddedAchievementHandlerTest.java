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
class FriendsAddedAchievementHandlerTest {

    @MockitoBean
    private AchievementRepository achievementRepository;

    @Autowired
    private FriendsAddedAchievementHandler handler;

    @BeforeEach
    void setUp() {
        handler = new FriendsAddedAchievementHandler(achievementRepository);
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

        when(achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, AchievementType.FRIENDS_ADDED, 1))
                .thenReturn(Optional.of(new Achievement("frn:achievement:1", userFrn, AchievementType.FRIENDS_ADDED, 1, null)));

        // Act
        handler.checkAchievement(userFrn, activities);

        // Assert - Osiągnięcie już istnieje, więc `save` nie powinno być wywołane
        verify(achievementRepository, never()).save(any(Achievement.class));
    }
}
