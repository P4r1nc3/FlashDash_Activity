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
import com.p4r1nc3.flashdash.activity.model.AchievementSchemaResponse;
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
        List<Achievement> achievements = achievementRepository.findByUserFrnOrderByAchievedAtDesc(userFrn);

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

        return achievements.stream()
                .map(achievement -> {
                    AchievementSchema schema = schemaMap.get(achievement.getSchemaFrn());
                    if (schema == null) {
                        logger.warn("Schema with FRN {} not found for achievement {}",
                                achievement.getSchemaFrn(), achievement.getAchievementFrn());
                        return null;
                    }

                    return EntityToResponseMapper.mapToAchievementResponse(achievement, schema);
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
        Optional<Achievement> achievementOptional = achievementRepository.findByAchievementFrnAndUserFrn(achievementFrn, userFrn);

        if (achievementOptional.isEmpty()) {
            logger.warn("Achievement not found for FRN: {}", achievementFrn);
            throw new FlashDashException(ErrorCode.E404001, "Achievement not found");
        }

        Achievement achievement = achievementOptional.get();

        // Fetch the schema for this achievement
        Optional<AchievementSchema> schemaOptional = schemaRepository.findBySchemaFrn(achievement.getSchemaFrn());

        if (schemaOptional.isEmpty()) {
            logger.warn("Schema not found for achievement: {}", achievementFrn);
            throw new FlashDashException(ErrorCode.E404001, "Achievement schema not found");
        }

        AchievementSchema schema = schemaOptional.get();

        return EntityToResponseMapper.mapToAchievementResponse(achievement, schema);
    }

    @Transactional(readOnly = true)
    public List<AchievementSchemaResponse> getAllAchievementSchemas() {
        List<AchievementSchema> schemas = schemaRepository.findAll();

        return schemas.stream()
                .map(EntityToResponseMapper::mapToAchievementSchemaResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AchievementSchemaResponse getAchievementSchema(String schemaFrn) {
        Optional<AchievementSchema> schemaOptional = schemaRepository.findBySchemaFrn(schemaFrn);

        if (schemaOptional.isEmpty()) {
            throw new FlashDashException(ErrorCode.E404002, "Achievement schema not found");
        }

        return EntityToResponseMapper.mapToAchievementSchemaResponse(schemaOptional.get());
    }

    @Transactional(readOnly = true)
    public List<AchievementSchemaResponse> getAchievementSchemasByType(AchievementType type) {
        List<AchievementSchema> schemas = schemaRepository.findByAchievementTypeOrderByLevelAsc(type);

        return schemas.stream()
                .map(EntityToResponseMapper::mapToAchievementSchemaResponse)
                .collect(Collectors.toList());
    }
}