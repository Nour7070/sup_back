package com.example.supervision.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.supervision.classes.ActivityLog;
import com.example.supervision.services.ActivityLogService;

@RestController
@RequestMapping("/api/activities")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping("/recent")
    public List<ActivityLog> getRecentActivities() {
        return activityLogService.getRecentActivities();
    }
}
