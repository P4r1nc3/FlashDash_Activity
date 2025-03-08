package com.flashdash.activity.util;

import com.flashdash.activity.model.Activity;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;

import java.time.LocalDateTime;
import java.util.UUID;

public class TestUtils {

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
}
