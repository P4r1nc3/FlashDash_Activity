package com.flashdash.activity.util;

import com.flashdash.activity.model.Achievement;
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
        Achievement achievement = new Achievement(
                "frn:flashdash:achievement:12345",
                "frn:user:67890",
                AchievementType.STREAK_ACHIEVEMENT,
                3,
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
        assertThat(response.getMaxLevel()).isEqualTo(5);
        assertThat(response.getRequiredCount()).isEqualTo(14);
        assertThat(response.getDescription()).isEqualTo("Achieve daily activity for a certain number of consecutive days.");
        assertThat(response.getAchievedAt()).isEqualTo(achievement.getAchievedAt().atOffset(ZoneOffset.UTC));
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

    @Test
    void shouldReturnZeroForInvalidLevel() {
        // Given
        Achievement achievement = new Achievement(
                "frn:flashdash:achievement:54321",
                "frn:user:67890",
                AchievementType.GAMES_PLAYED,
                10, // Invalid level (greater than max defined)
                LocalDateTime.of(2025, 3, 10, 15, 0)
        );

        // When
        AchievementResponse response = EntityToResponseMapper.mapToAchievementResponse(achievement);

        // Then
        assertThat(response.getRequiredCount()).isEqualTo(0);
    }
}
