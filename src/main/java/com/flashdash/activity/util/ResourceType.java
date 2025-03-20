package com.flashdash.activity.util;

public enum ResourceType {
    ACHIEVEMENT("achievement"),
    ACHIEVEMENT_SCHEMA("achievement-schema"),
    ACTIVITY("activity");

    private final String type;

    ResourceType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}