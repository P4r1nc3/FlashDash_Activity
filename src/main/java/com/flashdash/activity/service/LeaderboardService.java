package com.flashdash.activity.service;

import com.flashdash.activity.exception.FlashDashException;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.ActivityRepository;
import com.p4r1nc3.flashdash.activity.model.LeaderboardEntry;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.flashdash.activity.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private static final Logger logger = LoggerFactory.getLogger(LeaderboardService.class);
    private final ActivityRepository activityRepository;

    public LeaderboardService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public List<LeaderboardEntry> getLeaderboard(String criteria, List<String> includedUsers, int limit) {
        logger.info("Fetching leaderboard with criteria: {}, limit: {}", criteria, limit);

        List<Activity> activities = activityRepository.findAll();

        if (includedUsers != null && !includedUsers.isEmpty()) {
            activities = activities.stream()
                    .filter(a -> includedUsers.contains(a.getUserFrn()))
                    .collect(Collectors.toList());
        }

        Map<String, List<Activity>> groupedActivities = activities.stream()
                .collect(Collectors.groupingBy(Activity::getUserFrn));

        Map<String, Integer> userScores = new HashMap<>();
        if (includedUsers != null) {
            includedUsers.forEach(user -> userScores.put(user, 0));
        }

        groupedActivities.forEach((userFrn, userActivities) ->
                userScores.put(userFrn, calculateScore(userActivities, criteria))
        );

        List<LeaderboardEntry> leaderboard = userScores.entrySet().stream()
                .map(entry -> {
                    LeaderboardEntry entryObj = new LeaderboardEntry();
                    entryObj.setUserFrn(entry.getKey());
                    entryObj.setScore(entry.getValue());
                    return entryObj;
                })
                .sorted(Comparator.comparingInt(LeaderboardEntry::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        for (int i = 0; i < leaderboard.size(); i++) {
            leaderboard.get(i).setRank(i + 1);
        }

        logger.info("Generated leaderboard with {} entries.", leaderboard.size());
        return leaderboard;
    }

    private int calculateScore(List<Activity> activities, String criteria) {
        switch (criteria) {
            case "currentStreak":
                return calculateCurrentStreak(activities);
            case "totalGamesPlayed":
                return countActivitiesByType(activities, ActivityTypeEnum.GAME_FINISHED);
            case "totalDecksCreated":
                return countActivitiesByType(activities, ActivityTypeEnum.DECK_CREATED);
            case "totalQuestionsCreated":
                return countActivitiesByType(activities, ActivityTypeEnum.QUESTION_CREATED);
            case "totalFriendsAdded":
                return countActivitiesByType(activities, ActivityTypeEnum.FRIEND_INVITE_RESPONDED);
            default:
                throw new FlashDashException(ErrorCode.E400001, "Invalid leaderboard criteria: " + criteria);
        }
    }

    private int countActivitiesByType(List<Activity> activities, ActivityTypeEnum type) {
        return (int) activities.stream()
                .filter(a -> a.getActivity() == type)
                .count();
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
