package com.flashdash.activity.model;

import jakarta.persistence.*;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @Column(name = "activity_frn", nullable = false, length = 256)
    private String activityFrn;

    @Column(name = "user_frn", nullable = false, length = 256)
    private String userFrn;

    @Column(name = "target_frn", length = 256)
    private String targetFrn;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity", nullable = false, length = 64)
    private ActivityTypeEnum activity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Activity() {}

    public String getActivityFrn() {
        return activityFrn;
    }

    public String getUserFrn() {
        return userFrn;
    }

    public String getTargetFrn() {
        return targetFrn;
    }

    public ActivityTypeEnum getActivity() {
        return activity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setActivityFrn(String activityFrn) {
        this.activityFrn = activityFrn;
    }

    public void setUserFrn(String userFrn) {
        this.userFrn = userFrn;
    }

    public void setTargetFrn(String targetFrn) {
        this.targetFrn = targetFrn;
    }

    public void setActivity(ActivityTypeEnum activity) {
        this.activity = activity;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
