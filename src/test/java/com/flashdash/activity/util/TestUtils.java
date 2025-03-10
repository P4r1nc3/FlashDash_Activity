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

    public static List<Activity> createConsecutiveActivities(String userFrn, ActivityTypeEnum activityType, int days) {
        List<Activity> activities = new ArrayList<>();
        LocalDateTime currentDate = LocalDateTime.now();

        for (int i = 0; i < days; i++) {
            Activity activity = new Activity();
            activity.setActivityFrn("frn:flashdash:activity:" + UUID.randomUUID());
            activity.setUserFrn(userFrn);
            activity.setTargetFrn("frn:flashdash:streak-test");
            activity.setActivity(activityType);
            activity.setCreatedAt(currentDate.minusDays(i));
            activity.setUpdatedAt(currentDate.minusDays(i));
            activities.add(activity);
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

    public static List<Activity> createConsecutiveGameStreak(String userFrn, int daysStreak) {
        List<Activity> activities = new ArrayList<>();
        for (int i = 0; i < daysStreak; i++) {
            Activity activity = new Activity();
            activity.setActivityFrn("frn:flashdash:activity:" + UUID.randomUUID());
            activity.setUserFrn(userFrn);
            activity.setTargetFrn("frn:game:" + UUID.randomUUID());
            activity.setActivity(ActivityTypeEnum.GAME_FINISHED);
            activity.setCreatedAt(LocalDateTime.now().minusDays(i));
            activity.setUpdatedAt(LocalDateTime.now().minusDays(i));
            activities.add(activity);
        }
        return activities;
    }

    private static AchievementType getRandomAchievementType() {
        return ACHIEVEMENT_TYPES[RANDOM.nextInt(ACHIEVEMENT_TYPES.length)];
    }
}
