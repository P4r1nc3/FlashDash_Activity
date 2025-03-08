package com.flashdash.activity.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest.ActivityTypeEnum;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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
}
