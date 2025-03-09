package com.flashdash.activity.controller;

import com.flashdash.activity.service.ActivityStatisticsService;
import com.p4r1nc3.flashdash.activity.model.ActivityStatisticsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activity/statistics")
public class ActivityStatisticsController {

    private final ActivityStatisticsService activityStatisticsService;

    public ActivityStatisticsController(ActivityStatisticsService activityStatisticsService) {
        this.activityStatisticsService = activityStatisticsService;
    }

    @GetMapping
    public ResponseEntity<ActivityStatisticsResponse> getUserStatistics() {
        ActivityStatisticsResponse statistics = activityStatisticsService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }
}
