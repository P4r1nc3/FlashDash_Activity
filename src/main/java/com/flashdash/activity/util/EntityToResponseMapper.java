package com.flashdash.activity.util;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementSchema;
import com.p4r1nc3.flashdash.activity.model.AchievementResponse;
import com.p4r1nc3.flashdash.activity.model.AchievementSchemaResponse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class EntityToResponseMapper {

    public static AchievementResponse mapToAchievementResponse(Achievement achievement) {
        AchievementResponse response = new AchievementResponse();
        setAchievementFields(response, achievement);
        return response;
    }

    public static AchievementResponse mapToAchievementResponse(Achievement achievement, AchievementSchema schema) {
        AchievementResponse response = new AchievementResponse();

        // Set achievement fields
        setAchievementFields(response, achievement);

        // Set schema fields
        setSchemaFields(response, schema);

        return response;
    }

    public static AchievementSchemaResponse mapToAchievementSchemaResponse(AchievementSchema schema) {
        AchievementSchemaResponse response = new AchievementSchemaResponse();
        response.setSchemaId(extractId(schema.getSchemaFrn()));
        response.setSchemaFrn(schema.getSchemaFrn());
        response.setName(schema.getName());
        response.setDescription(schema.getDescription());
        response.setAchievementType(AchievementSchemaResponse.AchievementTypeEnum.fromValue(schema.getAchievementType().name()));
        response.setLevel(schema.getLevel());
        response.setThreshold(schema.getThreshold());

        return response;
    }

    private static void setAchievementFields(AchievementResponse response, Achievement achievement) {
        response.setAchievementFrn(achievement.getAchievementFrn());
        response.setAchievementId(extractId(achievement.getAchievementFrn()));
        response.setAchievementType(AchievementResponse.AchievementTypeEnum.fromValue(achievement.getAchievementType().name()));
        response.setLevel(achievement.getLevel());
        response.setAchievedAt(OffsetDateTime.of(achievement.getAchievedAt(), ZoneOffset.UTC));
    }

    private static void setSchemaFields(AchievementResponse response, AchievementSchema schema) {
        response.setSchemaFrn(schema.getSchemaFrn());
        response.setSchemaId(extractId(schema.getSchemaFrn()));
        response.setName(schema.getName());
        response.setDescription(schema.getDescription());
        response.setThreshold(schema.getThreshold());
    }

    public static String extractId(String frn) {
        if (frn != null && frn.contains(":")) {
            String[] parts = frn.split(":");
            return parts[parts.length - 1];
        }
        return frn;
    }
}