package com.flashdash.activity.service;

import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.service.achievements.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AchievementService {

    private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);

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
        logger.info("Checking achievements for user: {}", userFrn);
        List<Activity> activities = activityRepository.findByUserFrn(userFrn);
        logger.info("User {} has {} recorded activities.", userFrn, activities.size());
        achievementChain.checkAchievement(userFrn, activities);
        logger.info("Finished processing achievements for user: {}", userFrn);
    }
}
