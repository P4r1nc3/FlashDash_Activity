package com.flashdash.activity.repository;

import com.flashdash.activity.FlashDashActivityApplication;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.util.TestUtils;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest(classes = FlashDashActivityApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ActivityRepositoryTest {

    @Autowired
    private ActivityRepository activityRepository;

    @BeforeEach
    void setUp() {
        activityRepository.deleteAll();
    }

    @Test
    void shouldFindActivitiesByUserFrn() {
        // Arrange
        Activity activity1 = TestUtils.createActivity("frn:user:123", "frn:deck:456", ActivityTypeEnum.DECK_CREATED);
        Activity activity2 = TestUtils.createActivity("frn:user:123", "frn:game:789", ActivityTypeEnum.GAME_FINISHED);
        activityRepository.save(activity1);
        activityRepository.save(activity2);

        // Act
        List<Activity> activities = activityRepository.findByUserFrn("frn:user:123");

        // Assert
        assertThat(activities).hasSize(2);
        assertThat(activities).extracting(Activity::getUserFrn).containsOnly("frn:user:123");
        assertThat(activities).extracting(Activity::getActivity).containsExactlyInAnyOrder(ActivityTypeEnum.DECK_CREATED, ActivityTypeEnum.GAME_FINISHED);
    }

    @Test
    void shouldReturnEmptyListWhenNoActivitiesForUserFrn() {
        // Act
        List<Activity> activities = activityRepository.findByUserFrn("frn:user:nonexistent");

        // Assert
        assertThat(activities).isEmpty();
    }

    @Test
    void shouldSaveAndRetrieveActivity() {
        // Arrange
        Activity activity = TestUtils.createActivity("frn:user:321", "frn:deck:654", ActivityTypeEnum.DECK_UPDATED);
        activityRepository.save(activity);

        // Act
        List<Activity> foundActivities = activityRepository.findByUserFrn("frn:user:321");

        // Assert
        assertThat(foundActivities).hasSize(1);
        Activity savedActivity = foundActivities.get(0);
        assertThat(savedActivity.getUserFrn()).isEqualTo(activity.getUserFrn());
        assertThat(savedActivity.getTargetFrn()).isEqualTo(activity.getTargetFrn());
        assertThat(savedActivity.getActivity()).isEqualTo(activity.getActivity());
    }
}
