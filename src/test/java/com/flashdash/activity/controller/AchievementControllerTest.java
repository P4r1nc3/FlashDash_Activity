package com.flashdash.activity.controller;

import com.flashdash.activity.exception.ErrorCode;
import com.flashdash.activity.exception.FlashDashException;
import com.flashdash.activity.service.AchievementService;
import com.p4r1nc3.flashdash.activity.model.AchievementResponse;
import com.p4r1nc3.flashdash.activity.model.AchievementSchemaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AchievementControllerTest {

    @Autowired
    private AchievementController achievementController;

    @MockitoBean
    private AchievementService achievementService;

    private String userFrn;
    private String achievementFrn;

    @BeforeEach
    void setUp() {
        achievementService = mock(AchievementService.class);
        achievementController = new AchievementController(achievementService);
        userFrn = "frn:user:" + UUID.randomUUID();
        achievementFrn = "frn:flashdash:achievement:" + UUID.randomUUID();
    }

    @Test
    void shouldGetUserAchievementsSuccessfully() {
        // Arrange
        AchievementResponse achievement1 = new AchievementResponse();
        achievement1.setAchievementFrn("frn:flashdash:achievement:1");

        AchievementResponse achievement2 = new AchievementResponse();
        achievement2.setAchievementFrn("frn:flashdash:achievement:2");

        when(achievementService.getUserAchievements()).thenReturn(List.of(achievement1, achievement2));

        // Act
        ResponseEntity<List<AchievementResponse>> response = achievementController.getUserAchievements();

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull().hasSize(2);
        assertThat(response.getBody().get(0).getAchievementFrn()).isEqualTo("frn:flashdash:achievement:1");
        assertThat(response.getBody().get(1).getAchievementFrn()).isEqualTo("frn:flashdash:achievement:2");

        verify(achievementService, times(1)).getUserAchievements();
    }

    @Test
    void shouldReturnEmptyListWhenNoAchievementsFound() {
        // Arrange
        when(achievementService.getUserAchievements()).thenReturn(List.of());

        // Act
        ResponseEntity<List<AchievementResponse>> response = achievementController.getUserAchievements();

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();

        verify(achievementService, times(1)).getUserAchievements();
    }

    @Test
    void shouldThrowFlashDashExceptionWhenGetUserAchievementsFails() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E401001, "Token is missing or invalid"))
                .when(achievementService).getUserAchievements();

        // Act & Assert
        assertThatThrownBy(() -> achievementController.getUserAchievements())
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E401001)
                .hasMessage("Token is missing or invalid");

        verify(achievementService, times(1)).getUserAchievements();
    }

    @Test
    void shouldGetAchievementByFrnSuccessfully() {
        // Arrange
        AchievementResponse achievement = new AchievementResponse();
        achievement.setAchievementFrn(achievementFrn);

        when(achievementService.getAchievementByFrn(achievementFrn)).thenReturn(achievement);

        // Act
        ResponseEntity<AchievementResponse> response = achievementController.getAchievementByFrn(achievementFrn);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAchievementFrn()).isEqualTo(achievementFrn);

        verify(achievementService, times(1)).getAchievementByFrn(achievementFrn);
    }

    @Test
    void shouldThrowFlashDashExceptionWhenAchievementNotFound() {
        // Arrange
        doThrow(new FlashDashException(ErrorCode.E404001, "Achievement not found"))
                .when(achievementService).getAchievementByFrn(achievementFrn);

        // Act & Assert
        assertThatThrownBy(() -> achievementController.getAchievementByFrn(achievementFrn))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404001)
                .hasMessage("Achievement not found");

        verify(achievementService, times(1)).getAchievementByFrn(achievementFrn);
    }

    @Test
    void shouldGetAllAchievementSchemasSuccessfully() {
        // Arrange
        AchievementSchemaResponse schema1 = new AchievementSchemaResponse();
        schema1.setSchemaFrn("frn:flashdash:schema:1");

        AchievementSchemaResponse schema2 = new AchievementSchemaResponse();
        schema2.setSchemaFrn("frn:flashdash:schema:2");

        when(achievementService.getAllAchievementSchemas()).thenReturn(List.of(schema1, schema2));

        // Act
        ResponseEntity<List<AchievementSchemaResponse>> response = achievementController.getAllAchievementSchemas();

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull().hasSize(2);
        assertThat(response.getBody().get(0).getSchemaFrn()).isEqualTo("frn:flashdash:schema:1");
        assertThat(response.getBody().get(1).getSchemaFrn()).isEqualTo("frn:flashdash:schema:2");

        verify(achievementService, times(1)).getAllAchievementSchemas();
    }

    @Test
    void shouldReturnEmptyListWhenNoSchemasFound() {
        // Arrange
        when(achievementService.getAllAchievementSchemas()).thenReturn(List.of());

        // Act
        ResponseEntity<List<AchievementSchemaResponse>> response = achievementController.getAllAchievementSchemas();

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();

        verify(achievementService, times(1)).getAllAchievementSchemas();
    }

    @Test
    void shouldGetAchievementSchemaByFrnSuccessfully() {
        // Arrange
        String schemaFrn = "frn:flashdash:schema:" + UUID.randomUUID();
        AchievementSchemaResponse schema = new AchievementSchemaResponse();
        schema.setSchemaFrn(schemaFrn);
        schema.setName("Achievement Schema Test");
        schema.setDescription("Test Description");

        when(achievementService.getAchievementSchema(schemaFrn)).thenReturn(schema);

        // Act
        ResponseEntity<AchievementSchemaResponse> response = achievementController.getAchievementSchema(schemaFrn);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSchemaFrn()).isEqualTo(schemaFrn);
        assertThat(response.getBody().getName()).isEqualTo("Achievement Schema Test");
        assertThat(response.getBody().getDescription()).isEqualTo("Test Description");

        verify(achievementService, times(1)).getAchievementSchema(schemaFrn);
    }

    @Test
    void shouldThrowFlashDashExceptionWhenSchemaNotFound() {
        // Arrange
        String schemaFrn = "frn:flashdash:schema:" + UUID.randomUUID();

        doThrow(new FlashDashException(ErrorCode.E404002, "Achievement schema not found"))
                .when(achievementService).getAchievementSchema(schemaFrn);

        // Act & Assert
        assertThatThrownBy(() -> achievementController.getAchievementSchema(schemaFrn))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E404002)
                .hasMessage("Achievement schema not found");

        verify(achievementService, times(1)).getAchievementSchema(schemaFrn);
    }

    @Test
    void shouldThrowFlashDashExceptionWhenInvalidSchemaFrn() {
        // Arrange
        String invalidSchemaFrn = "invalid-frn-format";

        doThrow(new FlashDashException(ErrorCode.E400001, "Invalid schema FRN format"))
                .when(achievementService).getAchievementSchema(invalidSchemaFrn);

        // Act & Assert
        assertThatThrownBy(() -> achievementController.getAchievementSchema(invalidSchemaFrn))
                .isInstanceOf(FlashDashException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.E400001)
                .hasMessage("Invalid schema FRN format");

        verify(achievementService, times(1)).getAchievementSchema(invalidSchemaFrn);
    }
}