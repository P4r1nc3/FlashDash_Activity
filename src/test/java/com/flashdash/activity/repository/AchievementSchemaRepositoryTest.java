package com.flashdash.activity.repository;

import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AchievementSchemaRepositoryTest {

    @Autowired
    private AchievementSchemaRepository achievementSchemaRepository;

    @BeforeEach
    void setUp() {
        achievementSchemaRepository.deleteAll();
    }

    private AchievementSchema createTestSchema(AchievementType type, int level) {
        String schemaFrn = "frn:flashdash:achievementschema:" + type.name().toLowerCase() + ":" + level;
        String name = "Achievement " + type.name() + " Level " + level;
        String description = "Complete " + (level * 10) + " " + type.name() + " to earn this achievement";
        int threshold = level * 10;

        return new AchievementSchema(schemaFrn, type, level, threshold, name, description);
    }

    @Test
    void shouldFindBySchemaFrn() {
        // Arrange
        AchievementSchema schema = createTestSchema(AchievementType.GAMES_PLAYED, 1);
        achievementSchemaRepository.save(schema);

        // Act
        Optional<AchievementSchema> foundSchema = achievementSchemaRepository.findBySchemaFrn(schema.getSchemaFrn());

        // Assert
        assertThat(foundSchema).isPresent();
        assertThat(foundSchema.get().getSchemaFrn()).isEqualTo(schema.getSchemaFrn());
        assertThat(foundSchema.get().getAchievementType()).isEqualTo(schema.getAchievementType());
        assertThat(foundSchema.get().getLevel()).isEqualTo(schema.getLevel());
        assertThat(foundSchema.get().getName()).isEqualTo(schema.getName());
        assertThat(foundSchema.get().getDescription()).isEqualTo(schema.getDescription());
        assertThat(foundSchema.get().getThreshold()).isEqualTo(schema.getThreshold());
    }

    @Test
    void shouldReturnEmptyOptionalWhenSchemaFrnNotFound() {
        // Act
        Optional<AchievementSchema> foundSchema = achievementSchemaRepository.findBySchemaFrn(
                "frn:flashdash:achievementschema:nonexistent");

        // Assert
        assertThat(foundSchema).isEmpty();
    }

    @Test
    void shouldFindByAchievementTypeAndLevel() {
        // Arrange
        AchievementSchema schema = createTestSchema(AchievementType.GAMES_PLAYED, 2);
        achievementSchemaRepository.save(schema);

        // Act
        Optional<AchievementSchema> foundSchema = achievementSchemaRepository.findByAchievementTypeAndLevel(
                schema.getAchievementType(), schema.getLevel());

        // Assert
        assertThat(foundSchema).isPresent();
        assertThat(foundSchema.get().getAchievementType()).isEqualTo(schema.getAchievementType());
        assertThat(foundSchema.get().getLevel()).isEqualTo(schema.getLevel());
        assertThat(foundSchema.get().getThreshold()).isEqualTo(schema.getThreshold());
    }

    @Test
    void shouldReturnEmptyOptionalWhenAchievementTypeAndLevelNotFound() {
        // Act
        Optional<AchievementSchema> foundSchema = achievementSchemaRepository.findByAchievementTypeAndLevel(
                AchievementType.GAMES_PLAYED, 999);

        // Assert
        assertThat(foundSchema).isEmpty();
    }

    @Test
    void shouldFindByAchievementType() {
        // Arrange
        AchievementSchema schema1 = createTestSchema(AchievementType.GAMES_PLAYED, 1);
        AchievementSchema schema2 = createTestSchema(AchievementType.GAMES_PLAYED, 2);
        AchievementSchema schema3 = createTestSchema(AchievementType.DECKS_CREATED, 1);

        achievementSchemaRepository.save(schema1);
        achievementSchemaRepository.save(schema2);
        achievementSchemaRepository.save(schema3);

        // Act
        List<AchievementSchema> foundSchemas = achievementSchemaRepository.findByAchievementType(AchievementType.GAMES_PLAYED);

        // Assert
        assertThat(foundSchemas).hasSize(2);
        assertThat(foundSchemas).extracting(AchievementSchema::getAchievementType)
                .containsOnly(AchievementType.GAMES_PLAYED);
    }

    @Test
    void shouldReturnEmptyListWhenNoSchemasForAchievementType() {
        // Act
        List<AchievementSchema> foundSchemas = achievementSchemaRepository.findByAchievementType(AchievementType.FRIENDS_ADDED);

        // Assert
        assertThat(foundSchemas).isEmpty();
    }

    @Test
    void shouldFindByAchievementTypeOrderByLevelAsc() {
        // Arrange
        AchievementSchema schema1 = createTestSchema(AchievementType.GAMES_PLAYED, 3);
        AchievementSchema schema2 = createTestSchema(AchievementType.GAMES_PLAYED, 1);
        AchievementSchema schema3 = createTestSchema(AchievementType.GAMES_PLAYED, 2);
        AchievementSchema schema4 = createTestSchema(AchievementType.QUESTIONS_CREATED, 1);

        // Save in non-sequential order to test sorting
        achievementSchemaRepository.save(schema1);
        achievementSchemaRepository.save(schema2);
        achievementSchemaRepository.save(schema3);
        achievementSchemaRepository.save(schema4);

        // Act
        List<AchievementSchema> foundSchemas = achievementSchemaRepository.findByAchievementTypeOrderByLevelAsc(
                AchievementType.GAMES_PLAYED);

        // Assert
        assertThat(foundSchemas).hasSize(3);
        assertThat(foundSchemas).extracting(AchievementSchema::getLevel)
                .containsExactly(1, 2, 3);
    }

    @Test
    void shouldReturnEmptyListWhenNoSchemasForAchievementTypeOrdered() {
        // Act
        List<AchievementSchema> foundSchemas = achievementSchemaRepository.findByAchievementTypeOrderByLevelAsc(
                AchievementType.STREAK_ACHIEVEMENT);

        // Assert
        assertThat(foundSchemas).isEmpty();
    }

    @Test
    void shouldTestCacheOperations() {
        // Arrange
        AchievementSchema schema1 = createTestSchema(AchievementType.GAMES_PLAYED, 1);
        AchievementSchema schema2 = createTestSchema(AchievementType.GAMES_PLAYED, 2);
        achievementSchemaRepository.save(schema1);
        achievementSchemaRepository.save(schema2);

        List<AchievementSchema> schemas = achievementSchemaRepository.findAll();

        // Act
        AchievementSchema.updateCache(schemas);
        int cachedThreshold1 = AchievementSchema.getThreshold(AchievementType.GAMES_PLAYED, 1);
        int cachedThreshold2 = AchievementSchema.getThreshold(AchievementType.GAMES_PLAYED, 2);
        int nonExistentThreshold = AchievementSchema.getThreshold(AchievementType.STREAK_ACHIEVEMENT, 1);

        // Assert
        assertThat(cachedThreshold1).isEqualTo(10);
        assertThat(cachedThreshold2).isEqualTo(20);
        assertThat(nonExistentThreshold).isEqualTo(Integer.MAX_VALUE);
    }
}