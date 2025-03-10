package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public abstract class BaseAchievementHandler {

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
            nextHandler.checkAchievement(userFrn, activities);
        }
    }

    protected void grantAchievement(String userFrn, AchievementType type, int level) {
        if (achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, type, level).isEmpty()) {
            achievementRepository.save(new Achievement(
                    "frn:flashdash:achievement:" + UUID.randomUUID(),
                    userFrn,
                    type,
                    level,
                    LocalDateTime.now()
            ));
        }
    }
}
