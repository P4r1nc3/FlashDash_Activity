package com.flashdash.activity.service;

import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.service.achievements.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AchievementService {

    private final ActivityRepository activityRepository;

    public AchievementService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Async
    public void checkAchievementsForUser(String userFrn) {
        List<Activity> activities = activityRepository.findByUserFrn(userFrn);

        BaseAchievementHandler achievementChain = BaseAchievementHandler.link(
                new GamesPlayedAchievementHandler(),
                new DecksCreatedAchievementHandler(),
                new QuestionsCreatedAchievementHandler(),
                new FriendsAddedAchievementHandler(),
                new StreakAchievementHandler()
        );

        achievementChain.checkAchievement(userFrn, activities);
    }
}
