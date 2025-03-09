package com.flashdash.activity.controller;

import com.flashdash.activity.service.UserStatisticsService;
import com.p4r1nc3.flashdash.activity.model.UserStatisticsResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/activity/statistics")
public class UserStatisticsController {

    private final UserStatisticsService userStatisticsService;

    public UserStatisticsController(UserStatisticsService userStatisticsService) {
        this.userStatisticsService = userStatisticsService;
    }

    @GetMapping
    public ResponseEntity<UserStatisticsResponse> getUserStatistics() {
        UserStatisticsResponse statistics = userStatisticsService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }
}
