package com.flashdash.activity.service;

import com.flashdash.activity.exception.ErrorCode;
import com.flashdash.activity.exception.FlashDashException;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.util.FrnGenerator;
import com.flashdash.activity.util.ResourceType;
import com.flashdash.activity.util.UserContext;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);

    private final UserContext userContext;
    private final AchievementService achievementService;
    private final ActivityRepository activityRepository;

    public ActivityService(UserContext userContext,
                           AchievementService achievementService,
                           ActivityRepository activityRepository) {
        this.userContext = userContext;
        this.achievementService = achievementService;
        this.activityRepository = activityRepository;
    }

    public Activity logActivity(LogActivityRequest request) {
        String userFrn = userContext.getUserFrn();

        if (userFrn == null) {
            throw new FlashDashException(ErrorCode.E401001, "User is not authenticated");
        }

        Activity activity = new Activity();
        activity.setActivityFrn(FrnGenerator.generateFrn(ResourceType.ACTIVITY));
        activity.setUserFrn(userFrn);
        activity.setTargetFrn(request.getTargetFrn());
        activity.setActivity(request.getActivityType());
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());

        activityRepository.save(activity);
        logger.info("Activity logged for user {}: {}", userFrn, activity);
        achievementService.checkAchievementsForUser(userFrn);

        return activity;
    }

    public List<Activity> getUserActivities() {
        String userFrn = userContext.getUserFrn();
        return activityRepository.findByUserFrn(userFrn);
    }
}
