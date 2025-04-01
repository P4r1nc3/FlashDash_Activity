package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.AchievementSchemaRepository;
import com.flashdash.activity.service.api.NotificationService;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GamesPlayedAchievementHandler extends BaseAchievementHandler {

    private static final Logger logger = LoggerFactory.getLogger(GamesPlayedAchievementHandler.class);

    public GamesPlayedAchievementHandler(AchievementRepository achievementRepository,
                                         AchievementSchemaRepository schemaRepository,
                                         NotificationService notificationService) {
        super(achievementRepository, schemaRepository, notificationService);
    }

    @Override
    public void checkAchievement(String userFrn, List<Activity> activities) {
        long totalGames = activities.stream()
                .filter(a -> a.getActivity() == ActivityTypeEnum.GAME_FINISHED)
                .count();

        logger.info("Checking games played achievements for user {}. Total games played: {}", userFrn, totalGames);

        // Get achievement levels and thresholds from the database
        Map<Integer, Integer> levelsAndThresholds = getAchievementLevelsAndThresholds(AchievementType.GAMES_PLAYED);

        // Check each level
        for (Map.Entry<Integer, Integer> entry : levelsAndThresholds.entrySet()) {
            int level = entry.getKey();
            int threshold = entry.getValue();

            if (totalGames >= threshold) {
                logger.info("User {} achieved Games Played Level {} Achievement (threshold: {})",
                        userFrn, level, threshold);
                grantAchievement(userFrn, AchievementType.GAMES_PLAYED, level);
            }
        }

        checkNext(userFrn, activities);
    }
}