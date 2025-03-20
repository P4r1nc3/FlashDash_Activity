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
public class QuestionsCreatedAchievementHandler extends BaseAchievementHandler {

    private static final Logger logger = LoggerFactory.getLogger(QuestionsCreatedAchievementHandler.class);

    public QuestionsCreatedAchievementHandler(AchievementRepository achievementRepository,
                                              AchievementSchemaRepository schemaRepository) {
        super(achievementRepository, schemaRepository);
    }

    @Override
    public void checkAchievement(String userFrn, List<Activity> activities) {
        long totalQuestions = activities.stream()
                .filter(a -> a.getActivity() == ActivityTypeEnum.QUESTION_CREATED)
                .count();

        logger.info("Checking questions created achievements for user {}. Total questions created: {}", userFrn, totalQuestions);

        // Get achievement levels and thresholds from the database
        Map<Integer, Integer> levelsAndThresholds = getAchievementLevelsAndThresholds(AchievementType.QUESTIONS_CREATED);

        // Check each level
        for (Map.Entry<Integer, Integer> entry : levelsAndThresholds.entrySet()) {
            int level = entry.getKey();
            int threshold = entry.getValue();

            if (totalQuestions >= threshold) {
                logger.info("User {} achieved Questions Created Level {} Achievement (threshold: {})",
                        userFrn, level, threshold);
                grantAchievement(userFrn, AchievementType.QUESTIONS_CREATED, level);
            }
        }

        checkNext(userFrn, activities);
    }
}