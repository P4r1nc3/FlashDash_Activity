package com.flashdash.activity.util;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class TestUtils {

    private static final AchievementType[] ACHIEVEMENT_TYPES = AchievementType.values();
    private static final Random RANDOM = new Random();

    public static Activity createActivity(String userFrn, String targetFrn, ActivityTypeEnum activityType) {
        return createActivity(userFrn, targetFrn, activityType, LocalDateTime.now());
    }

    public static Activity createActivity(String userFrn, String targetFrn, ActivityTypeEnum activityType, LocalDateTime createdAt) {
        Activity activity = new Activity();
        activity.setActivityFrn("frn:flashdash:activity:" + UUID.randomUUID());
        activity.setUserFrn(userFrn);
        activity.setTargetFrn(targetFrn);
        activity.setActivity(activityType);
        activity.setCreatedAt(createdAt);
        activity.setUpdatedAt(createdAt);
        return activity;
    }

    public static Achievement createAchievement() {
        return createAchievement("frn:user:" + UUID.randomUUID(), getRandomAchievementType(), RANDOM.nextInt(5) + 1);
    }

    public static Achievement createAchievement(String userFrn, AchievementType type, int level) {
        Achievement achievement = new Achievement();
        achievement.setAchievementFrn("frn:flashdash:achievement:" + UUID.randomUUID());
        achievement.setUserFrn(userFrn);
        achievement.setAchievementType(type);
        achievement.setLevel(level);
        achievement.setAchievedAt(LocalDateTime.now());
        return achievement;
    }

    public static List<Activity> createConsecutiveGameStreak(String userFrn, int streakLength) {
        List<Activity> activities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < streakLength; i++) {
            activities.add(createActivity(userFrn, "frn:game:" + UUID.randomUUID(), ActivityTypeEnum.GAME_FINISHED, now.minusDays(i)));
        }

        return activities;
    }

    public static List<Activity> createGameActivities(String userFrn, int gameCount) {
        List<Activity> activities = new ArrayList<>();
        for (int i = 0; i < gameCount; i++) {
            activities.add(createActivity(userFrn, "frn:game:" + UUID.randomUUID(), ActivityTypeEnum.GAME_FINISHED));
        }
        return activities;
    }

    public static List<Activity> createQuestionActivities(String userFrn, int questionCount) {
        List<Activity> activities = new ArrayList<>();
        for (int i = 0; i < questionCount; i++) {
            activities.add(createActivity(userFrn, "frn:question:" + UUID.randomUUID(), ActivityTypeEnum.QUESTION_CREATED));
        }
        return activities;
    }

    private static AchievementType getRandomAchievementType() {
        return ACHIEVEMENT_TYPES[RANDOM.nextInt(ACHIEVEMENT_TYPES.length)];
    }
}
