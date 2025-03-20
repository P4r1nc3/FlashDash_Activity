package com.flashdash.activity.service;

import com.flashdash.activity.exception.ErrorCode;
import com.flashdash.activity.exception.FlashDashException;
import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.model.Activity;
import com.flashdash.activity.repository.AchievementRepository;
import com.flashdash.activity.repository.AchievementSchemaRepository;
import com.flashdash.activity.repository.ActivityRepository;
import com.flashdash.activity.service.achievements.*;
import com.flashdash.activity.util.EntityToResponseMapper;
import com.flashdash.activity.util.UserContext;
import com.p4r1nc3.flashdash.activity.model.AchievementResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AchievementService {

    private static final Logger logger = LoggerFactory.getLogger(AchievementService.class);

    private final UserContext userContext;
    private final BaseAchievementHandler achievementChain;
    private final AchievementRepository achievementRepository;
    private final AchievementSchemaRepository schemaRepository;
    private final ActivityRepository activityRepository;

    public AchievementService(UserContext userContext,
                              GamesPlayedAchievementHandler gamesPlayedAchievementHandler,
                              DecksCreatedAchievementHandler decksCreatedAchievementHandler,
                              QuestionsCreatedAchievementHandler questionsCreatedAchievementHandler,
                              FriendsAddedAchievementHandler friendsAddedAchievementHandler,
                              StreakAchievementHandler streakAchievementHandler,
                              AchievementRepository achievementRepository,
                              AchievementSchemaRepository schemaRepository,
                              ActivityRepository activityRepository) {
        this.userContext = userContext;
        this.achievementChain = BaseAchievementHandler.link(
                gamesPlayedAchievementHandler,
                decksCreatedAchievementHandler,
                questionsCreatedAchievementHandler,
                friendsAddedAchievementHandler,
                streakAchievementHandler
        );
        this.achievementRepository = achievementRepository;
        this.schemaRepository = schemaRepository;
        this.activityRepository = activityRepository;
    }

    @Async
    public void checkAchievementsForUser(String userFrn) {
        logger.info("Checking achievements for user: {}", userFrn);
        List<Activity> activities = activityRepository.findByUserFrn(userFrn);
        logger.info("User {} has {} recorded activities.", userFrn, activities.size());
        achievementChain.checkAchievement(userFrn, activities);
        logger.info("Finished processing achievements for user: {}", userFrn);
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getUserAchievements() {
        String userFrn = userContext.getUserFrn();

        if (userFrn == null) {
            throw new FlashDashException(ErrorCode.E401001, "User is not authenticated");
        }

        logger.info("Fetching achievements for user: {}", userFrn);
        List<Achievement> achievements = achievementRepository.findByUserFrn(userFrn);

        if (achievements.isEmpty()) {
            return List.of();
        }

        // Collect all schema FRNs needed
        List<String> schemaFrns = achievements.stream()
                .map(Achievement::getSchemaFrn)
                .collect(Collectors.toList());

        // Fetch all needed schemas in one query
        List<AchievementSchema> schemas = schemaRepository.findAllById(schemaFrns);

        // Map schemas by FRN for quick lookup
        Map<String, AchievementSchema> schemaMap = schemas.stream()
                .collect(Collectors.toMap(AchievementSchema::getSchemaFrn, schema -> schema));

        // Get maximum levels for each achievement type for UI display
        Map<com.flashdash.activity.model.AchievementType, Integer> maxLevelMap = schemas.stream()
                .collect(Collectors.groupingBy(
                        AchievementSchema::getAchievementType,
                        Collectors.mapping(AchievementSchema::getLevel, Collectors.maxBy(Integer::compareTo))
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));

        return achievements.stream()
                .map(achievement -> {
                    AchievementSchema schema = schemaMap.get(achievement.getSchemaFrn());
                    if (schema == null) {
                        logger.warn("Schema with FRN {} not found for achievement {}",
                                achievement.getSchemaFrn(), achievement.getAchievementFrn());
                        return null;
                    }

                    // Get the max level for this achievement type
                    Integer maxLevel = maxLevelMap.getOrDefault(achievement.getAchievementType(), 5); // Default to 5 if not found

                    return EntityToResponseMapper.mapToAchievementResponse(
                            achievement, schema, maxLevel);
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AchievementResponse getAchievementByFrn(String achievementFrn) {
        String userFrn = userContext.getUserFrn();

        if (userFrn == null) {
            throw new FlashDashException(ErrorCode.E401001, "User is not authenticated");
        }

        logger.info("Fetching achievement with FRN: {} for user: {}", achievementFrn, userFrn);
        Optional<Achievement> achievementOpt = achievementRepository.findByAchievementFrnAndUserFrn(achievementFrn, userFrn);

        if (achievementOpt.isEmpty()) {
            logger.warn("Achievement not found for FRN: {}", achievementFrn);
            throw new FlashDashException(ErrorCode.E404001, "Achievement not found");
        }

        Achievement achievement = achievementOpt.get();

        // Fetch the schema for this achievement
        Optional<AchievementSchema> schemaOpt = schemaRepository.findById(achievement.getSchemaFrn());

        if (schemaOpt.isEmpty()) {
            logger.warn("Schema not found for achievement: {}", achievementFrn);
            throw new FlashDashException(ErrorCode.E404001, "Achievement schema not found");
        }

        AchievementSchema schema = schemaOpt.get();

        // Get max level for this achievement type
        Integer maxLevel = schemaRepository.findByAchievementTypeOrderByLevelAsc(achievement.getAchievementType())
                .stream()
                .map(AchievementSchema::getLevel)
                .max(Integer::compareTo)
                .orElse(5); // Default to 5 if not found

        return EntityToResponseMapper.mapToAchievementResponse(achievement, schema, maxLevel);
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getAllAchievementSchemas() {
        List<AchievementSchema> schemas = schemaRepository.findAll();

        Map<AchievementType, Integer> maxLevelMap = schemas.stream()
                .collect(Collectors.groupingBy(
                        AchievementSchema::getAchievementType,
                        Collectors.mapping(AchievementSchema::getLevel, Collectors.maxBy(Integer::compareTo))
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));

        return schemas.stream()
                .map(schema -> {
                    Integer maxLevel = maxLevelMap.getOrDefault(schema.getAchievementType(), 5);
                    return EntityToResponseMapper.mapSchemaToAchievementResponse(schema, maxLevel);
                })
                .collect(Collectors.toList());
    }
}