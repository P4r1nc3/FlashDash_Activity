package com.flashdash.activity.service.achievements;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.AchievementSchemaRepository;
import com.flashdash.activity.util.FrnGenerator;
import com.flashdash.activity.util.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

public abstract class BaseAchievementHandler {

    private static final Logger logger = LoggerFactory.getLogger(BaseAchievementHandler.class);

    protected final AchievementRepository achievementRepository;
    protected final AchievementSchemaRepository schemaRepository;
    protected BaseAchievementHandler nextHandler;

    public BaseAchievementHandler(AchievementRepository achievementRepository,
                                  AchievementSchemaRepository schemaRepository) {
        this.achievementRepository = achievementRepository;
        this.schemaRepository = schemaRepository;
    }

    public static BaseAchievementHandler link(BaseAchievementHandler first, BaseAchievementHandler... chain) {
        BaseAchievementHandler head = first;
        for (BaseAchievementHandler nextInChain : chain) {
            head.nextHandler = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    public abstract void checkAchievement(String userFrn, List<Activity> activities);

    protected void checkNext(String userFrn, List<Activity> activities) {
        if (nextHandler != null) {
            logger.info("Passing achievement check to next handler for user {}", userFrn);
            nextHandler.checkAchievement(userFrn, activities);
        } else {
            logger.info("No more handlers left in the chain for user {}", userFrn);
        }
    }

    protected void grantAchievement(String userFrn, AchievementType type, int level) {
        if (achievementRepository.findByUserFrnAndAchievementTypeAndLevel(userFrn, type, level).isEmpty()) {
            // Find the schema for this achievement type and level
            Optional<AchievementSchema> schemaOpt = schemaRepository.findByAchievementTypeAndLevel(type, level);

            if (schemaOpt.isEmpty()) {
                logger.error("No schema found for achievement type {} and level {}. Cannot grant achievement to user {}.",
                        type, level, userFrn);
                return;
            }

            AchievementSchema schema = schemaOpt.get();
            String schemaFrn = schema.getSchemaFrn();

            Achievement achievement = new Achievement(
                    FrnGenerator.generateFrn(ResourceType.ACHIEVEMENT),
                    userFrn,
                    type,
                    level,
                    schemaFrn,
                    LocalDateTime.now()
            );
            achievementRepository.save(achievement);

            logger.info("Granted achievement: {} (Level {}) - '{}' to user {}",
                    type, level, schema.getName(), userFrn);
        } else {
            logger.info("User {} already has achievement {} (Level {}), skipping", userFrn, type, level);
        }
    }

    protected Map<Integer, Integer> getAchievementLevelsAndThresholds(AchievementType type) {
        // First try to get from cache
        Map<Integer, Integer> result = new TreeMap<>();
        for (int level = 1; level <= 10; level++) { // Assuming max 10 levels
            int threshold = AchievementSchema.getThreshold(type, level);
            if (threshold != Integer.MAX_VALUE) {
                result.put(level, threshold);
            }
        }

        // If cache is empty, get from database
        if (result.isEmpty()) {
            List<AchievementSchema> schemas = schemaRepository.findByAchievementType(type);
            return schemas.stream()
                    .collect(Collectors.toMap(AchievementSchema::getLevel, AchievementSchema::getThreshold));
        }

        return result;
    }
}