package com.flashdash.activity.service;

import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.util.UserContext;
import com.p4r1nc3.flashdash.activity.model.ActivityStatisticsResponse;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityStatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityStatisticsService.class);

    private final ActivityRepository activityRepository;
    private final UserContext userContext;

    public ActivityStatisticsService(ActivityRepository activityRepository, UserContext userContext) {
        this.activityRepository = activityRepository;
        this.userContext = userContext;
    }

    public ActivityStatisticsResponse getUserStatistics() {
        String userFrn = userContext.getUserFrn();
        logger.info("Fetching statistics for user: {}", userFrn);

        List<Activity> activities = activityRepository.findByUserFrn(userFrn);
        logger.info("User {} has {} activities.", userFrn, activities.size());

        int totalGamesPlayed = (int) activities.stream()
                .filter(a -> a.getActivity() == ActivityTypeEnum.GAME_FINISHED)
                .count();

        int totalDecksCreated = (int) activities.stream()
                .filter(a -> a.getActivity() == ActivityTypeEnum.DECK_CREATED)
                .count();

        int totalQuestionsCreated = (int) activities.stream()
                .filter(a -> a.getActivity() == ActivityTypeEnum.QUESTION_CREATED)
                .count();

        int totalFriendsAdded = (int) activities.stream()
                .filter(a -> a.getActivity() == ActivityTypeEnum.FRIEND_INVITE_RESPONDED)
                .count();

        int streak = calculateCurrentStreak(activities);

        logger.info("User {} statistics -> Games: {}, Decks: {}, Questions: {}, Friends: {}, Streak: {}",
                userFrn, totalGamesPlayed, totalDecksCreated, totalQuestionsCreated, totalFriendsAdded, streak);

        ActivityStatisticsResponse response = new ActivityStatisticsResponse();
        response.setCurrentStreak(streak);
        response.setTotalGamesPlayed(totalGamesPlayed);
        response.setTotalDecksCreated(totalDecksCreated);
        response.setTotalQuestionsCreated(totalQuestionsCreated);
        response.setTotalFriendsAdded(totalFriendsAdded);

        return response;
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
