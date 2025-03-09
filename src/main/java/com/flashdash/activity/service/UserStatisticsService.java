package com.flashdash.activity.service;

import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.util.UserContext;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;
import com.p4r1nc3.flashdash.activity.model.UserStatisticsResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserStatisticsService {

    private final ActivityRepository activityRepository;
    private final UserContext userContext;

    public UserStatisticsService(ActivityRepository activityRepository, UserContext userContext) {
        this.activityRepository = activityRepository;
        this.userContext = userContext;
    }

    public UserStatisticsResponse getUserStatistics() {
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

        UserStatisticsResponse userStatisticsResponse = new UserStatisticsResponse();
        userStatisticsResponse.setCurrentStreak(streak);
        userStatisticsResponse.setTotalGamesPlayed(totalGamesPlayed);
        userStatisticsResponse.setTotalDecksCreated(totalDecksCreated);
        userStatisticsResponse.setTotalQuestionsCreated(totalQuestionsCreated);
        userStatisticsResponse.setTotalFriendsAdded(totalFriendsAdded);

        return userStatisticsResponse;
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
