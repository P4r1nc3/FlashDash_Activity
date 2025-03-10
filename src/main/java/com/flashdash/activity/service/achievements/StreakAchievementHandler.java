package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Component
public class StreakAchievementHandler extends BaseAchievementHandler {

    private static final Logger logger = LoggerFactory.getLogger(StreakAchievementHandler.class);
    private static final int[] STREAK_LEVELS = {3, 7, 14, 30, 50};

    public StreakAchievementHandler(AchievementRepository achievementRepository) {
        super(achievementRepository);
    }

    @Override
    public void checkAchievement(String userFrn, List<Activity> activities) {
        int streak = calculateCurrentStreak(activities);

        logger.info("Checking streak achievements for user {}. Current streak: {}", userFrn, streak);

        for (int i = 0; i < STREAK_LEVELS.length; i++) {
            if (streak >= STREAK_LEVELS[i]) {
                logger.info("User {} achieved Streak Level {}", userFrn, i + 1);
                grantAchievement(userFrn, AchievementType.STREAK_ACHIEVEMENT, i + 1);
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
