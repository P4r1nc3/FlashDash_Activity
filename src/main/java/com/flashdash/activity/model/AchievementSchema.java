package com.flashdash.activity.model;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

@Entity
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

    public String getSchemaFrn() {
        return schemaFrn;
    }

    public AchievementType getAchievementType() {
        return achievementType;
    }

    public int getLevel() {
        return level;
    }

    public int getThreshold() {
        return threshold;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setSchemaFrn(String schemaFrn) {
        this.schemaFrn = schemaFrn;
    }

    public void setAchievementType(AchievementType achievementType) {
        this.achievementType = achievementType;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
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
