package com.flashdash.activity.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "achievements")
public class Achievement {

    @Id
    @Column(name = "achievement_frn", nullable = false, length = 256)
    private String achievementFrn;

    @Column(name = "user_frn", nullable = false, length = 256)
    private String userFrn;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_type", nullable = false, length = 64)
    private AchievementType achievementType;

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "schema_frn", nullable = false, length = 256)
    private String schemaFrn;

    @Column(name = "achieved_at", nullable = false)
    private LocalDateTime achievedAt;

    public Achievement() {}

    public Achievement(String achievementFrn,
                       String userFrn,
                       AchievementType achievementType,
                       int level,
                       String schemaFrn,
                       LocalDateTime achievedAt) {
        this.achievementFrn = achievementFrn;
        this.userFrn = userFrn;
        this.achievementType = achievementType;
        this.level = level;
        this.schemaFrn = schemaFrn;
        this.achievedAt = achievedAt;
    }
}
