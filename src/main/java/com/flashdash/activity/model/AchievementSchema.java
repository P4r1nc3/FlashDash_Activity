package com.flashdash.activity.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@Table(name = "achievement_schemas")
public class AchievementSchema {

    @Id
    @Column(name = "schema_frn", nullable = false, length = 256)
    private String schemaFrn;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_type", nullable = false, length = 64)
    private AchievementType achievementType;

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "threshold", nullable = false)
    private int threshold;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    public AchievementSchema() {}

    public AchievementSchema(String schemaFrn, AchievementType achievementType, int level, int threshold, String name, String description) {
        this.schemaFrn = schemaFrn;
        this.achievementType = achievementType;
        this.level = level;
        this.threshold = threshold;
        this.name = name;
        this.description = description;
    }

    private static final Map<AchievementType, Map<Integer, Integer>> SCHEMA_CACHE = new HashMap<>();

    public static int getThreshold(AchievementType type, int level) {
        return SCHEMA_CACHE.getOrDefault(type, Map.of())
                .getOrDefault(level, Integer.MAX_VALUE);
    }

    public static void updateCache(Iterable<AchievementSchema> schemas) {
        SCHEMA_CACHE.clear();
        for (AchievementSchema schema : schemas) {
            SCHEMA_CACHE
                    .computeIfAbsent(schema.getAchievementType(), k -> new HashMap<>())
                    .put(schema.getLevel(), schema.getThreshold());
        }
    }
}