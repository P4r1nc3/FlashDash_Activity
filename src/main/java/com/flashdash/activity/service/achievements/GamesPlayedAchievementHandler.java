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
public class GamesPlayedAchievementHandler extends BaseAchievementHandler {

    private static final Logger logger = LoggerFactory.getLogger(GamesPlayedAchievementHandler.class);
    private static final int[] GAME_LEVELS = {10, 50, 100, 500, 1000};

    public GamesPlayedAchievementHandler(AchievementRepository achievementRepository) {
        super(achievementRepository);
    }

    @Override
    public void checkAchievement(String userFrn, List<Activity> activities) {
        long totalGames = activities.stream()
                .filter(a -> a.getActivity() == ActivityTypeEnum.GAME_FINISHED)
                .count();

        logger.info("Checking games played achievements for user {}. Total games played: {}", userFrn, totalGames);

        for (int i = 0; i < GAME_LEVELS.length; i++) {
            if (totalGames >= GAME_LEVELS[i]) {
                logger.info("User {} achieved Games Played Level {} Achievement", userFrn, i + 1);
                grantAchievement(userFrn, AchievementType.GAMES_PLAYED, i + 1);
            }
        }

        checkNext(userFrn, activities);
    }
}
