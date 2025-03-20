package com.flashdash.activity.util;

import com.flashdash.activity.model.Achievement;
import com.flashdash.activity.model.AchievementSchema;
import com.flashdash.activity.model.AchievementType;
import com.p4r1nc3.flashdash.activity.model.AchievementResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EntityToResponseMapperTest {

    @Test
    void shouldMapAchievementToAchievementResponse() {
        // Given
        String schemaFrn = "frn:flashdash:achievement-schema:streak-level3";
        Achievement achievement = new Achievement(
                "frn:flashdash:achievement:12345",
                "frn:user:67890",
                AchievementType.STREAK_ACHIEVEMENT,
                3,
                schemaFrn,
                LocalDateTime.of(2025, 3, 10, 15, 0)
        );

        // When
        AchievementResponse response = EntityToResponseMapper.mapToAchievementResponse(achievement);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAchievementId()).isEqualTo("12345");
        assertThat(response.getAchievementFrn()).isEqualTo("frn:flashdash:achievement:12345");
        assertThat(response.getAchievementType()).isEqualTo(AchievementResponse.AchievementTypeEnum.STREAK_ACHIEVEMENT);
        assertThat(response.getLevel()).isEqualTo(3);
        assertThat(response.getAchievedAt()).isEqualTo(achievement.getAchievedAt().atOffset(ZoneOffset.UTC));
    }

    @Test
    void shouldMapAchievementWithSchemaToAchievementResponse() {
        // Given
        String schemaFrn = "frn:flashdash:achievement-schema:streak-level3";
        Achievement achievement = new Achievement(
                "frn:flashdash:achievement:12345",
                "frn:user:67890",
                AchievementType.STREAK_ACHIEVEMENT,
                3,
                schemaFrn,
                LocalDateTime.of(2025, 3, 10, 15, 0)
        );

        AchievementSchema schema = new AchievementSchema(
                schemaFrn,
                AchievementType.STREAK_ACHIEVEMENT,
                3,
                14,
                "Daily Streak Level 3",
                "Achieve daily activity for a certain number of consecutive days."
        );

        // When
        AchievementResponse response = EntityToResponseMapper.mapToAchievementResponse(achievement, schema);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAchievementId()).isEqualTo("12345");
        assertThat(response.getAchievementFrn()).isEqualTo("frn:flashdash:achievement:12345");
        assertThat(response.getAchievementType()).isEqualTo(AchievementResponse.AchievementTypeEnum.STREAK_ACHIEVEMENT);
        assertThat(response.getLevel()).isEqualTo(3);
        assertThat(response.getAchievedAt()).isEqualTo(achievement.getAchievedAt().atOffset(ZoneOffset.UTC));
        assertThat(response.getSchemaFrn()).isEqualTo(schemaFrn);
        assertThat(response.getSchemaId()).isEqualTo("streak-level3");
        assertThat(response.getName()).isEqualTo("Daily Streak Level 3");
        assertThat(response.getDescription()).isEqualTo("Achieve daily activity for a certain number of consecutive days.");
        assertThat(response.getThreshold()).isEqualTo(14);
    }

    @Test
    void shouldMapSchemaToAchievementResponse() {
        // Given
        String schemaFrn = "frn:flashdash:achievement-schema:games-level2";
        AchievementSchema schema = new AchievementSchema(
                schemaFrn,
                AchievementType.GAMES_PLAYED,
                2,
                10,
                "Games Played Level 2",
                "Play a certain number of games."
        );

        // When
        AchievementResponse response = EntityToResponseMapper.mapSchemaToAchievementResponse(schema);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSchemaFrn()).isEqualTo(schemaFrn);
        assertThat(response.getSchemaId()).isEqualTo("games-level2");
        assertThat(response.getAchievementType()).isEqualTo(AchievementResponse.AchievementTypeEnum.GAMES_PLAYED);
        assertThat(response.getLevel()).isEqualTo(2);
        assertThat(response.getName()).isEqualTo("Games Played Level 2");
        assertThat(response.getDescription()).isEqualTo("Play a certain number of games.");
        assertThat(response.getThreshold()).isEqualTo(10);
    }

    @Test
    void shouldExtractIdFromFrn() {
        // Given
        String frn = "frn:flashdash:achievement:98765";

        // When
        String extractedId = EntityToResponseMapper.extractId(frn);

        // Then
        assertThat(extractedId).isEqualTo("98765");
    }

    @Test
    void shouldReturnOriginalStringIfFrnIsInvalid() {
        // Given
        String invalidFrn = "invalid-frn-format";

        // When
        String extractedId = EntityToResponseMapper.extractId(invalidFrn);

        // Then
        assertThat(extractedId).isEqualTo(invalidFrn);
    }
}