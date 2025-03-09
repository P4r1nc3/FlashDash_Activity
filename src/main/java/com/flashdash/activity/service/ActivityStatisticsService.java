package com.flashdash.activity.service;

import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.util.UserContext;
import com.p4r1nc3.flashdash.activity.model.ActivityStatisticsResponse;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityStatisticsService {

    private final ActivityRepository activityRepository;
    private final UserContext userContext;

    public ActivityStatisticsService(ActivityRepository activityRepository, UserContext userContext) {
        this.activityRepository = activityRepository;
        this.userContext = userContext;
    }

    public ActivityStatisticsResponse getUserStatistics() {
        String userFrn = userContext.getUserFrn();
        List<Activity> activities = activityRepository.findByUserFrn(userFrn);

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

        ActivityStatisticsResponse activityStatisticsResponse = new ActivityStatisticsResponse();
        activityStatisticsResponse.setCurrentStreak(streak);
        activityStatisticsResponse.setTotalGamesPlayed(totalGamesPlayed);
        activityStatisticsResponse.setTotalDecksCreated(totalDecksCreated);
        activityStatisticsResponse.setTotalQuestionsCreated(totalQuestionsCreated);
        activityStatisticsResponse.setTotalFriendsAdded(totalFriendsAdded);

        return activityStatisticsResponse;
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
