package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.AchievementSchemaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class StreakAchievementHandler extends BaseAchievementHandler {

    private static final Logger logger = LoggerFactory.getLogger(StreakAchievementHandler.class);

    public StreakAchievementHandler(AchievementRepository achievementRepository,
                                    AchievementSchemaRepository schemaRepository) {
        super(achievementRepository, schemaRepository);
    }

    @Override
    public void checkAchievement(String userFrn, List<Activity> activities) {
        int streak = calculateCurrentStreak(activities);

        logger.info("Checking streak achievements for user {}. Current streak: {}", userFrn, streak);

        // Get achievement levels and thresholds from the database
        Map<Integer, Integer> levelsAndThresholds = getAchievementLevelsAndThresholds(AchievementType.STREAK_ACHIEVEMENT);

        // Check each level
        for (Map.Entry<Integer, Integer> entry : levelsAndThresholds.entrySet()) {
            int level = entry.getKey();
            int threshold = entry.getValue();

            if (streak >= threshold) {
                logger.info("User {} achieved Streak Level {} Achievement (threshold: {})",
                        userFrn, level, threshold);
                grantAchievement(userFrn, AchievementType.STREAK_ACHIEVEMENT, level);
            }
        }

        checkNext(userFrn, activities);
    }

    private int calculateCurrentStreak(List<Activity> activities) {
        List<LocalDate> activeDays = activities.stream()
                .map(a -> a.getCreatedAt().toLocalDate())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        int streak = 0;
        LocalDate previousDay = null;

        for (LocalDate day : activeDays) {
            if (previousDay == null || previousDay.plusDays(1).equals(day)) {
                streak++;
            } else {
                streak = 1;
            }
            previousDay = day;
        }
        return streak;
    }
}