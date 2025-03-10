package com.flashdash.activity.util;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

public class TestUtils {

    private static final AchievementType[] ACHIEVEMENT_TYPES = AchievementType.values();
    private static final Random RANDOM = new Random();

    public static Activity createActivity(String userFrn, String targetFrn, ActivityTypeEnum activityType) {
        Activity activity = new Activity();
        activity.setActivityFrn("frn:flashdash:activity:" + UUID.randomUUID());
        activity.setUserFrn(userFrn);
        activity.setTargetFrn(targetFrn);
        activity.setActivity(activityType);
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        return activity;
    }

    public static Achievement createAchievement() {
        Achievement achievement = new Achievement();
        achievement.setAchievementFrn("frn:flashdash:achievement:" + UUID.randomUUID());
        achievement.setUserFrn("frn:user:" + UUID.randomUUID());
        achievement.setAchievementType(getRandomAchievementType());
        achievement.setLevel(RANDOM.nextInt(5) + 1);
        achievement.setAchievedAt(LocalDateTime.now());
        return achievement;
    }

    private static AchievementType getRandomAchievementType() {
        return ACHIEVEMENT_TYPES[RANDOM.nextInt(ACHIEVEMENT_TYPES.length)];
    }
}
