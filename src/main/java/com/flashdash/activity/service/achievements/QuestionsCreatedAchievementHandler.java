package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionsCreatedAchievementHandler extends BaseAchievementHandler {

    private static final Logger logger = LoggerFactory.getLogger(QuestionsCreatedAchievementHandler.class);
    private static final int[] QUESTION_LEVELS = {10, 100, 500, 1000, 5000};

    public QuestionsCreatedAchievementHandler(AchievementRepository achievementRepository) {
        super(achievementRepository);
    }

    @Override
    public void checkAchievement(String userFrn, List<Activity> activities) {
        long totalQuestions = activities.stream()
                .filter(a -> a.getActivity() == ActivityTypeEnum.QUESTION_CREATED)
                .count();

        logger.info("Checking questions created achievements for user {}. Total questions created: {}", userFrn, totalQuestions);

        for (int i = 0; i < QUESTION_LEVELS.length; i++) {
            if (totalQuestions >= QUESTION_LEVELS[i]) {
                logger.info("User {} achieved Questions Created Level {} Achievement", userFrn, i + 1);
                grantAchievement(userFrn, AchievementType.QUESTIONS_CREATED, i + 1);
            }
        }

        checkNext(userFrn, activities);
    }
}
