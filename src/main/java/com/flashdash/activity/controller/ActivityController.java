package com.flashdash.activity.controller;

import com.flashdash.activity.model.Activity;
import com.flashdash.activity.service.ActivityService;
import com.p4r1nc3.flashdash.activity.model.LogActivityRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping
    public ResponseEntity<Void> logActivity(@RequestBody LogActivityRequest request) {
        activityService.logActivity(request);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<List<Activity>> getUserActivities() {
        return ResponseEntity.ok(activityService.getUserActivities());
    }
}
