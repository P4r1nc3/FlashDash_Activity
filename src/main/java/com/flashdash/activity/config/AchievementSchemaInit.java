package com.flashdash.activity.config;

import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.repository.AchievementSchemaRepository;
import com.flashdash.activity.util.ResourceType;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AchievementSchemaInit {

    private static final Logger logger = LoggerFactory.getLogger(AchievementSchemaInit.class);

    private final AchievementSchemaRepository schemaRepository;

    public AchievementSchemaInit(AchievementSchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    // Default achievement configurations
    private static final Map<AchievementType, int[]> DEFAULT_ACHIEVEMENT_THRESHOLDS = Map.of(
            AchievementType.STREAK_ACHIEVEMENT, new int[]{3, 7, 14, 30, 50},
            AchievementType.GAMES_PLAYED, new int[]{10, 50, 100, 500, 1000},
            AchievementType.DECKS_CREATED, new int[]{3, 10, 25, 50, 100},
            AchievementType.QUESTIONS_CREATED, new int[]{10, 100, 500, 1000, 5000},
            AchievementType.FRIENDS_ADDED, new int[]{3, 10, 25, 50, 100}
    );

    // Achievement names/titles shown to users
    private static final Map<AchievementType, String[]> ACHIEVEMENT_NAMES = Map.of(
            AchievementType.STREAK_ACHIEVEMENT, new String[]{
                    "Daily Learner",
                    "Consistent Student",
                    "Study Streak",
                    "Learning Devotee",
                    "Knowledge Warrior"
            },
            AchievementType.GAMES_PLAYED, new String[]{
                    "Game Explorer",
                    "Game Enthusiast",
                    "Game Master",
                    "Game Champion",
                    "Game Legend"
            },
            AchievementType.DECKS_CREATED, new String[]{
                    "Deck Creator",
                    "Deck Designer",
                    "Deck Architect",
                    "Deck Maestro",
                    "Deck Virtuoso"
            },
            AchievementType.QUESTIONS_CREATED, new String[]{
                    "Question Maker",
                    "Question Crafter",
                    "Question Expert",
                    "Question Authority",
                    "Question Guru"
            },
            AchievementType.FRIENDS_ADDED, new String[]{
                    "Social Starter",
                    "Friend Collector",
                    "Social Networker",
                    "Community Builder",
                    "Social Luminary"
            }
    );

    // Descriptions that explain the achievement to users
    private static final Map<AchievementType, String> ACHIEVEMENT_DESCRIPTIONS = Map.of(
            AchievementType.STREAK_ACHIEVEMENT, "maintaining your daily learning streak",
            AchievementType.GAMES_PLAYED, "completing quiz games",
            AchievementType.DECKS_CREATED, "creating flashcard decks",
            AchievementType.QUESTIONS_CREATED, "creating flashcard questions",
            AchievementType.FRIENDS_ADDED, "connecting with friends"
    );

    // FRN name fragments to make FRNs more readable
    private static final Map<AchievementType, String> FRN_NAME_FRAGMENTS = Map.of(
            AchievementType.STREAK_ACHIEVEMENT, "streak",
            AchievementType.GAMES_PLAYED, "games",
            AchievementType.DECKS_CREATED, "decks",
            AchievementType.QUESTIONS_CREATED, "questions",
            AchievementType.FRIENDS_ADDED, "friends"
    );

    @PostConstruct
    @Transactional
    public void initializeAchievementSchemas() {
        logger.info("Initializing achievement schemas");

        if (schemaRepository.count() == 0) {
            List<AchievementSchema> schemas = new ArrayList<>();

            for (Map.Entry<AchievementType, int[]> entry : DEFAULT_ACHIEVEMENT_THRESHOLDS.entrySet()) {
                AchievementType type = entry.getKey();
                int[] thresholds = entry.getValue();
                String[] names = ACHIEVEMENT_NAMES.get(type);
                String baseDescription = ACHIEVEMENT_DESCRIPTIONS.get(type);
                String frnFragment = FRN_NAME_FRAGMENTS.get(type);

                for (int i = 0; i < thresholds.length; i++) {
                    int level = i + 1;
                    int threshold = thresholds[i];
                    String name = names[i];

                    // Simple description format as requested
                    String description = String.format("Awarded for %s %d times",
                            baseDescription, threshold);

                    // Create a readable and meaningful FRN
                    String schemaFrn = String.format("frn:flashdash:achievement-schema:%s-level%d",
                            frnFragment, level);

                    schemas.add(new AchievementSchema(schemaFrn, type, level, threshold, name, description));
                }
            }

            schemaRepository.saveAll(schemas);
            logger.info("Saved {} achievement schemas", schemas.size());

            // Update the cache
            AchievementSchema.updateCache(schemas);
        } else {
            logger.info("Achievement schemas already exist, updating cache");
            AchievementSchema.updateCache(schemaRepository.findAll());
        }
    }
}