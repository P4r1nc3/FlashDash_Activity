package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.util.FrnGenerator;
import com.flashdash.activity.util.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public abstract class BaseAchievementHandler {

    private static final Logger logger = LoggerFactory.getLogger(BaseAchievementHandler.class);

    protected final AchievementRepository achievementRepository;
    protected BaseAchievementHandler nextHandler;

    public BaseAchievementHandler(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    public static BaseAchievementHandler link(BaseAchievementHandler first, BaseAchievementHandler... chain) {
        BaseAchievementHandler head = first;
        for (BaseAchievementHandler nextInChain : chain) {
            head.nextHandler = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    public abstract void checkAchievement(String userFrn, List<Activity> activities);

    protected void checkNext(String userFrn, List<Activity> activities) {
        if (nextHandler != null) {
            logger.info("Passing achievement check to next handler for user {}", userFrn);
            nextHandler.checkAchievement(userFrn, activities);
        } else {
            logger.info("No more handlers left in the chain for user {}", userFrn);
        }
    }

    protected void grantAchievement(String userFrn, AchievementType type, int level) {
        if (achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, type, level).isEmpty()) {
            Achievement achievement = new Achievement(
                    FrnGenerator.generateFrn(ResourceType.ACHIEVEMENT),
                    userFrn,
                    type,
                    level,
                    LocalDateTime.now()
            );
            achievementRepository.save(achievement);
            logger.info("Granted achievement: {} (Level {}) to user {}", type, level, userFrn);
        } else {
            logger.info("User {} already has achievement {} (Level {}), skipping", userFrn, type, level);
        }
    }
}
