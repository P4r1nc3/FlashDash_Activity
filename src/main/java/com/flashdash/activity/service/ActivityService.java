package com.flashdash.activity.service;

import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.util.UserContext;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);
    private final ActivityRepository activityRepository;
    private final UserContext userContext;

    public ActivityService(ActivityRepository activityRepository, UserContext userContext) {
        this.activityRepository = activityRepository;
        this.userContext = userContext;
    }

    public Activity logActivity(LogActivityRequest request) {
        String userFrn = userContext.getUserFrn();

        if (userFrn == null) {
            throw new IllegalStateException("User is not authenticated");
        }

        Activity activity = new Activity();
        activity.setActivityFrn("frn:flashdash:activity:" + UUID.randomUUID());
        activity.setUserFrn(userFrn);
        activity.setTargetFrn(request.getTargetFrn());
        activity.setActivity(request.getActivityType());
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());

        activityRepository.save(activity);
        logger.info("Activity logged for user {}: {}", userFrn, activity);
        return activity;
    }

    public List<Activity> getUserActivities() {
        String userFrn = userContext.getUserFrn();
        return activityRepository.findByUserFrn(userFrn);
    }
}
