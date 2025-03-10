package com.flashdash.activity.service;

import com.flashdash.activity.exception.ErrorCode;
import com.flashdash.activity.exception.FlashDashException;
import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.service.achievements.*;
import com.flashdash.activity.util.EntityToResponseMapper;
import com.flashdash.activity.util.UserContext;
import com.p4r1nc3.flashdash.activity.model.AchievementResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);

    private final UserContext userContext;
    private final BaseAchievementHandler achievementChain;
    private final AchievementRepository achievementRepository;
    private final ActivityRepository activityRepository;

    public AchievementService(UserContext userContext,
                              GamesPlayedAchievementHandler gamesPlayedAchievementHandler,
                              DecksCreatedAchievementHandler decksCreatedAchievementHandler,
                              QuestionsCreatedAchievementHandler questionsCreatedAchievementHandler,
                              FriendsAddedAchievementHandler friendsAddedAchievementHandler,
                              StreakAchievementHandler streakAchievementHandler,
                              AchievementRepository achievementRepository,
                              ActivityRepository activityRepository) {
        this.userContext = userContext;
        this.achievementChain = BaseAchievementHandler.link(
                gamesPlayedAchievementHandler,
                decksCreatedAchievementHandler,
                questionsCreatedAchievementHandler,
                friendsAddedAchievementHandler,
                streakAchievementHandler
        );
        this.achievementRepository = achievementRepository;
        this.activityRepository = activityRepository;
    }

    @Async
    public void checkAchievementsForUser(String userFrn) {
        logger.info("Checking achievements for user: {}", userFrn);
        List<Activity> activities = activityRepository.findByUserFrn(userFrn);
        logger.info("User {} has {} recorded activities.", userFrn, activities.size());
        achievementChain.checkAchievement(userFrn, activities);
        logger.info("Finished processing achievements for user: {}", userFrn);
    }

    public List<AchievementResponse> getUserAchievements() {
        String userFrn = userContext.getUserFrn();

        if (userFrn == null) {
            throw new FlashDashException(ErrorCode.E401001, "User is not authenticated");
        }

        logger.info("Fetching achievements for user: {}", userFrn);
        List<Achievement> achievements = achievementRepository.findByUserFrn(userFrn);
        return achievements.stream()
                .map(EntityToResponseMapper::mapToAchievementResponse)
                .collect(Collectors.toList());
    }

    public AchievementResponse getAchievementByFrn(String achievementFrn) {
        String userFrn = userContext.getUserFrn();

        if (userFrn == null) {
            throw new FlashDashException(ErrorCode.E401001, "User is not authenticated");
        }

        logger.info("Fetching achievement with FRN: {} for user: {}", achievementFrn, userFrn);
        Optional<Achievement> achievement = achievementRepository.findByAchievementFrnAndUserFrn(achievementFrn, userFrn);

        return achievement.map(EntityToResponseMapper::mapToAchievementResponse)
                .orElseThrow(() -> {
                    logger.warn("Achievement not found for FRN: {}", achievementFrn);
                    return new FlashDashException(ErrorCode.E404001, "Achievement not found");
                });
    }
}
