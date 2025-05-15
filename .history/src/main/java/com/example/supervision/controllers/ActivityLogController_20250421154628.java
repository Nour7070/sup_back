package com.example.supervision.controllers;

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
