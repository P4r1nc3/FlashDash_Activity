package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.AchievementSchemaRepository;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FriendsAddedAchievementHandler extends BaseAchievementHandler {

    private static final Logger logger = LoggerFactory.getLogger(FriendsAddedAchievementHandler.class);

    public FriendsAddedAchievementHandler(AchievementRepository achievementRepository,
                                          AchievementSchemaRepository schemaRepository) {
        super(achievementRepository, schemaRepository);
    }

    @Override
    public void checkAchievement(String userFrn, List<Activity> activities) {
        long totalFriends = activities.stream()
                .filter(a -> a.getActivity() == ActivityTypeEnum.FRIEND_INVITE_RESPONDED)
                .count();

        logger.info("Checking friends achievements for user {}. Total friends added: {}", userFrn, totalFriends);

        // Get achievement levels and thresholds from the database
        Map<Integer, Integer> levelsAndThresholds = getAchievementLevelsAndThresholds(AchievementType.FRIENDS_ADDED);

        // Check each level
        for (Map.Entry<Integer, Integer> entry : levelsAndThresholds.entrySet()) {
            int level = entry.getKey();
            int threshold = entry.getValue();

            if (totalFriends >= threshold) {
                logger.info("User {} achieved Friends Added Level {} Achievement (threshold: {})",
                        userFrn, level, threshold);
                grantAchievement(userFrn, AchievementType.FRIENDS_ADDED, level);
            }
        }

        checkNext(userFrn, activities);
    }
}