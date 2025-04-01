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
public class DecksCreatedAchievementHandler extends BaseAchievementHandler {

    private static final Logger logger = LoggerFactory.getLogger(DecksCreatedAchievementHandler.class);

    public DecksCreatedAchievementHandler(AchievementRepository achievementRepository,
                                          AchievementSchemaRepository schemaRepository,
                                          NotificationService notificationService) {
        super(achievementRepository, schemaRepository, notificationService);
    }

    @Override
    public void checkAchievement(String userFrn, List<Activity> activities) {
        long totalDecks = activities.stream()
                .filter(a -> a.getActivity() == ActivityTypeEnum.DECK_CREATED)
                .count();

        logger.info("Checking deck achievements for user {}. Total decks created: {}", userFrn, totalDecks);

        // Get achievement levels and thresholds from the database
        Map<Integer, Integer> levelsAndThresholds = getAchievementLevelsAndThresholds(AchievementType.DECKS_CREATED);

        // Check each level
        for (Map.Entry<Integer, Integer> entry : levelsAndThresholds.entrySet()) {
            int level = entry.getKey();
            int threshold = entry.getValue();

            if (totalDecks >= threshold) {
                logger.info("User {} achieved Decks Created Level {} Achievement (threshold: {})",
                        userFrn, level, threshold);
                grantAchievement(userFrn, AchievementType.DECKS_CREATED, level);
            }
        }

        checkNext(userFrn, activities);
    }
}