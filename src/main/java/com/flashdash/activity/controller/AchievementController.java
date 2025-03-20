package com.flashdash.activity.controller;

import com.flashdash.activity.service.AchievementService;
import com.p4r1nc3.flashdash.activity.model.AchievementResponse;
import com.p4r1nc3.flashdash.activity.model.AchievementSchemaResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @GetMapping
    public ResponseEntity<List<AchievementResponse>> getUserAchievements() {
        List<AchievementResponse> achievements = achievementService.getUserAchievements();
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/{achievementFrn}")
    public ResponseEntity<AchievementResponse> getAchievementByFrn(@PathVariable String achievementFrn) {
        AchievementResponse achievement = achievementService.getAchievementByFrn(achievementFrn);
        return ResponseEntity.ok(achievement);
    }

    @GetMapping("/schemas")
    public ResponseEntity<List<AchievementSchemaResponse>> getAllAchievementSchemas() {
        List<AchievementSchemaResponse> schemas = achievementService.getAllAchievementSchemas();
        return ResponseEntity.ok(schemas);
    }

    @GetMapping("/schemas/{schemaFrn}")
    public ResponseEntity<AchievementSchemaResponse> getAchievementSchema(@PathVariable String schemaFrn) {
        AchievementSchemaResponse schemas = achievementService.getAchievementSchema(schemaFrn);
        return ResponseEntity.ok(schemas);
    }
}
