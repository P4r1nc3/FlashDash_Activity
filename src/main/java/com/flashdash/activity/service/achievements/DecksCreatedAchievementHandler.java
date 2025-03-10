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
public class DecksCreatedAchievementHandler extends BaseAchievementHandler {

    private static final Logger logger = LoggerFactory.getLogger(DecksCreatedAchievementHandler.class);
    private static final int[] DECK_LEVELS = {3, 10, 25, 50, 100};

    public DecksCreatedAchievementHandler(AchievementRepository achievementRepository) {
        super(achievementRepository);
    }

    @Override
    public void checkAchievement(String userFrn, List<Activity> activities) {
        long totalDecks = activities.stream()
                .filter(a -> a.getActivity() == ActivityTypeEnum.DECK_CREATED)
                .count();

        logger.info("Checking deck achievements for user {}. Total decks created: {}", userFrn, totalDecks);

        for (int i = 0; i < DECK_LEVELS.length; i++) {
            if (totalDecks >= DECK_LEVELS[i]) {
                logger.info("User {} achieved Deck Level {} Achievement", userFrn, i + 1);
                grantAchievement(userFrn, AchievementType.DECKS_CREATED, i + 1);
            }
        }

        checkNext(userFrn, activities);
    }
}
