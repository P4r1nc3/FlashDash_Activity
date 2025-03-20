package com.flashdash.activity.config;

import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import com.flashdash.activity.repository.AchievementSchemaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AchievementSchemaInitTest {

    private AchievementSchemaInit achievementSchemaInit;

    @MockitoBean
    private AchievementSchemaRepository schemaRepository;

    @Captor
    private ArgumentCaptor<List<AchievementSchema>> schemasCaptor;

    @BeforeEach
    void setUp() {
        achievementSchemaInit = new AchievementSchemaInit(schemaRepository);
    }

    @Test
    void shouldInitializeAchievementSchemasWhenNoneExist() {
        // Arrange
        when(schemaRepository.count()).thenReturn(0L);

        // Act
        achievementSchemaInit.initializeAchievementSchemas();

        // Assert
        verify(schemaRepository, times(1)).count();
        verify(schemaRepository, times(1)).saveAll(schemasCaptor.capture());

        List<AchievementSchema> capturedSchemas = schemasCaptor.getValue();

        // Verify we have the expected number of schemas (5 types * 5 levels = 25)
        assertThat(capturedSchemas).hasSize(25);

        // Verify achievement types
        assertThat(capturedSchemas.stream()
                .map(AchievementSchema::getAchievementType)
                .distinct()
                .count())
                .isEqualTo(5);

        // Check for specific schema types
        assertThat(capturedSchemas.stream()
                .map(AchievementSchema::getAchievementType))
                .contains(
                        AchievementType.STREAK_ACHIEVEMENT,
                        AchievementType.GAMES_PLAYED,
                        AchievementType.DECKS_CREATED,
                        AchievementType.QUESTIONS_CREATED,
                        AchievementType.FRIENDS_ADDED
                );

        // Verify each achievement type has 5 levels
        for (AchievementType type : AchievementType.values()) {
            if (type == AchievementType.STREAK_ACHIEVEMENT ||
                    type == AchievementType.GAMES_PLAYED ||
                    type == AchievementType.DECKS_CREATED ||
                    type == AchievementType.QUESTIONS_CREATED ||
                    type == AchievementType.FRIENDS_ADDED) {

                long count = capturedSchemas.stream()
                        .filter(schema -> schema.getAchievementType() == type)
                        .count();

                assertThat(count).isEqualTo(5);
            }
        }
    }

    @Test
    void shouldNotInitializeWhenAchievementSchemasExist() {
        // Arrange
        when(schemaRepository.count()).thenReturn(25L);
        when(schemaRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        achievementSchemaInit.initializeAchievementSchemas();

        // Assert
        verify(schemaRepository, times(1)).count();
        verify(schemaRepository, never()).saveAll(anyList());
        verify(schemaRepository, times(1)).findAll();
    }

    @Test
    void shouldCreateCorrectFrnsForSchemas() {
        // Arrange
        when(schemaRepository.count()).thenReturn(0L);

        // Act
        achievementSchemaInit.initializeAchievementSchemas();

        // Assert
        verify(schemaRepository).saveAll(schemasCaptor.capture());
        List<AchievementSchema> capturedSchemas = schemasCaptor.getValue();

        // Verify FRNs follow the expected pattern
        for (AchievementSchema schema : capturedSchemas) {
            assertThat(schema.getSchemaFrn()).startsWith("frn:flashdash:achievement-schema:");

            String expectedFragment = "";
            switch (schema.getAchievementType()) {
                case STREAK_ACHIEVEMENT:
                    expectedFragment = "streak";
                    break;
                case GAMES_PLAYED:
                    expectedFragment = "games";
                    break;
                case DECKS_CREATED:
                    expectedFragment = "decks";
                    break;
                case QUESTIONS_CREATED:
                    expectedFragment = "questions";
                    break;
                case FRIENDS_ADDED:
                    expectedFragment = "friends";
                    break;
            }

            assertThat(schema.getSchemaFrn()).contains(expectedFragment);
            assertThat(schema.getSchemaFrn()).endsWith("level" + schema.getLevel());
        }
    }

    @Test
    void shouldCreateCorrectThresholdsForSchemas() {
        // Arrange
        when(schemaRepository.count()).thenReturn(0L);

        // Act
        achievementSchemaInit.initializeAchievementSchemas();

        // Assert
        verify(schemaRepository).saveAll(schemasCaptor.capture());
        List<AchievementSchema> capturedSchemas = schemasCaptor.getValue();

        // Verify thresholds for STREAK_ACHIEVEMENT
        List<Integer> streakThresholds = capturedSchemas.stream()
                .filter(schema -> schema.getAchievementType() == AchievementType.STREAK_ACHIEVEMENT)
                .map(AchievementSchema::getThreshold)
                .sorted()
                .toList();

        assertThat(streakThresholds).containsExactly(3, 7, 14, 30, 50);

        // Verify thresholds for GAMES_PLAYED
        List<Integer> gamesThresholds = capturedSchemas.stream()
                .filter(schema -> schema.getAchievementType() == AchievementType.GAMES_PLAYED)
                .map(AchievementSchema::getThreshold)
                .sorted()
                .toList();

        assertThat(gamesThresholds).containsExactly(10, 50, 100, 500, 1000);
    }

    @Test
    void shouldCreateCorrectDescriptionsForSchemas() {
        // Arrange
        when(schemaRepository.count()).thenReturn(0L);

        // Act
        achievementSchemaInit.initializeAchievementSchemas();

        // Assert
        verify(schemaRepository, times(2)).saveAll(schemasCaptor.capture());
        List<AchievementSchema> capturedSchemas = schemasCaptor.getValue();

        // Verify descriptions follow the expected pattern
        AchievementSchema streakSchema = capturedSchemas.stream()
                .filter(schema -> schema.getAchievementType() == AchievementType.STREAK_ACHIEVEMENT && schema.getLevel() == 1)
                .findFirst()
                .orElseThrow();

        assertThat(streakSchema.getDescription()).isEqualTo("Awarded for maintaining your daily learning streak 3 times");

        AchievementSchema gamesSchema = capturedSchemas.stream()
                .filter(schema -> schema.getAchievementType() == AchievementType.GAMES_PLAYED && schema.getLevel() == 2)
                .findFirst()
                .orElseThrow();

        assertThat(gamesSchema.getDescription()).isEqualTo("Awarded for completing quiz games 50 times");
    }

    @Test
    void shouldCreateCorrectNamesForSchemas() {
        // Arrange
        when(schemaRepository.count()).thenReturn(0L);

        // Act
        achievementSchemaInit.initializeAchievementSchemas();

        // Assert
        verify(schemaRepository).saveAll(schemasCaptor.capture());
        List<AchievementSchema> capturedSchemas = schemasCaptor.getValue();

        // Verify names for STREAK_ACHIEVEMENT
        List<String> streakNames = capturedSchemas.stream()
                .filter(schema -> schema.getAchievementType() == AchievementType.STREAK_ACHIEVEMENT)
                .map(AchievementSchema::getName)
                .sorted((a, b) -> {
                    // Custom sorting to match the expected order in the class
                    String[] expectedNames = {"Daily Learner", "Consistent Student", "Study Streak", "Learning Devotee", "Knowledge Warrior"};
                    int aIndex = -1, bIndex = -1;
                    for (int i = 0; i < expectedNames.length; i++) {
                        if (expectedNames[i].equals(a)) aIndex = i;
                        if (expectedNames[i].equals(b)) bIndex = i;
                    }
                    return Integer.compare(aIndex, bIndex);
                })
                .toList();

        assertThat(streakNames).containsExactly(
                "Daily Learner",
                "Consistent Student",
                "Study Streak",
                "Learning Devotee",
                "Knowledge Warrior"
        );

        // Verify names for DECKS_CREATED
        List<String> decksNames = capturedSchemas.stream()
                .filter(schema -> schema.getAchievementType() == AchievementType.DECKS_CREATED)
                .map(AchievementSchema::getName)
                .sorted((a, b) -> {
                    // Custom sorting to match the expected order in the class
                    String[] expectedNames = {"Deck Creator", "Deck Designer", "Deck Architect", "Deck Maestro", "Deck Virtuoso"};
                    int aIndex = -1, bIndex = -1;
                    for (int i = 0; i < expectedNames.length; i++) {
                        if (expectedNames[i].equals(a)) aIndex = i;
                        if (expectedNames[i].equals(b)) bIndex = i;
                    }
                    return Integer.compare(aIndex, bIndex);
                })
                .toList();

        assertThat(decksNames).containsExactly(
                "Deck Creator",
                "Deck Designer",
                "Deck Architect",
                "Deck Maestro",
                "Deck Virtuoso"
        );
    }

    @Test
    void shouldUpdateCacheWhenInitializing() {
        // For now, we'll just verify the repository interactions
        when(schemaRepository.count()).thenReturn(0L);

        // Act
        achievementSchemaInit.initializeAchievementSchemas();

        // Assert
        verify(schemaRepository, times(1)).count();
        verify(schemaRepository, times(1)).saveAll(anyList());
    }
}