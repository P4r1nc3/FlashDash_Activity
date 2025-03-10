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
    private final BaseAchievementHandler achievementChain;

    public AchievementService(ActivityRepository activityRepository,
                              GamesPlayedAchievementHandler gamesPlayedAchievementHandler,
                              DecksCreatedAchievementHandler decksCreatedAchievementHandler,
                              QuestionsCreatedAchievementHandler questionsCreatedAchievementHandler,
                              FriendsAddedAchievementHandler friendsAddedAchievementHandler,
                              StreakAchievementHandler streakAchievementHandler) {
        this.activityRepository = activityRepository;

        this.achievementChain = BaseAchievementHandler.link(
                gamesPlayedAchievementHandler,
                decksCreatedAchievementHandler,
                questionsCreatedAchievementHandler,
                friendsAddedAchievementHandler,
                streakAchievementHandler
        );
    }

    @Async
    public void checkAchievementsForUser(String userFrn) {
        List<Activity> activities = activityRepository.findByUserFrn(userFrn);
        achievementChain.checkAchievement(userFrn, activities);
    }
}
