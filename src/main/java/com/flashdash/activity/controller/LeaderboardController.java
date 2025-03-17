package com.flashdash.activity.controller;

import com.flashdash.activity.service.LeaderboardService;
import com.p4r1nc3.flashdash.activity.model.LeaderboardEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leaderboards")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboards(
            @RequestParam String criteria,
            @RequestParam(required = false) List<String> includedUsers,
            @RequestParam(defaultValue = "10") int limit) {

        return ResponseEntity.ok(leaderboardService.getLeaderboard(criteria, includedUsers, limit));
    }
}
