package com.flashdash.activity.util;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementType;
import com.p4r1nc3.flashdash.activity.model.AchievementResponse;

public class EntityToResponseMapper {

    public static AchievementResponse mapToAchievementResponse(Achievement achievement) {
        return new AchievementResponse()
                .achievementId(extractId(achievement.getAchievementFrn()))
                .achievementFrn(achievement.getAchievementFrn())
                .achievementType(AchievementResponse.AchievementTypeEnum.fromValue(achievement.getAchievementType().name()))
                .level(achievement.getLevel())
                .maxLevel(getMaxLevelForType(achievement.getAchievementType()))
                .requiredCount(getRequiredCountForLevel(achievement.getAchievementType(), achievement.getLevel()))
                .description(getDescriptionForType(achievement.getAchievementType()))
                .achievedAt(achievement.getAchievedAt().atOffset(java.time.ZoneOffset.UTC));
    }

    private static int getMaxLevelForType(AchievementType achievementType) {
        return switch (achievementType) {
            case STREAK_ACHIEVEMENT -> 5;
            case GAMES_PLAYED -> 5;
            case DECKS_CREATED -> 5;
            case QUESTIONS_CREATED -> 5;
            case FRIENDS_ADDED -> 5;
        };
    }

    private static int getRequiredCountForLevel(AchievementType achievementType, int level) {
        int[] levels = switch (achievementType) {
            case STREAK_ACHIEVEMENT -> new int[]{3, 7, 14, 30, 50};
            case GAMES_PLAYED -> new int[]{10, 50, 100, 500, 1000};
            case DECKS_CREATED -> new int[]{3, 10, 25, 50, 100};
            case QUESTIONS_CREATED -> new int[]{10, 100, 500, 1000, 5000};
            case FRIENDS_ADDED -> new int[]{3, 10, 25, 50, 100};
        };
        return (level > 0 && level <= levels.length) ? levels[level - 1] : 0;
    }

    private static String getDescriptionForType(AchievementType achievementType) {
        return switch (achievementType) {
            case STREAK_ACHIEVEMENT -> "Achieve daily activity for a certain number of consecutive days.";
            case GAMES_PLAYED -> "Play a specified number of games.";
            case DECKS_CREATED -> "Create a specified number of decks.";
            case QUESTIONS_CREATED -> "Create a specified number of questions.";
            case FRIENDS_ADDED -> "Add a specified number of friends.";
        };
    }

    public static String extractId(String frn) {
        if (frn != null && frn.contains(":")) {
            String[] parts = frn.split(":");
            return parts[parts.length - 1];
        }
        return frn;
    }
}
