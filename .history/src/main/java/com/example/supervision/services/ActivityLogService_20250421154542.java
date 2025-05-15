package com.example.supervision.services;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public ActivityLogService(ActivityLogRepository repository) {
        this.repository = repository;
    }

    public void log(String type, String description) {
        ActivityLog activity = new ActivityLog();
        activity.setType(type);
        activity.setDescription(description);
        activity.setTimestamp(LocalDateTime.now());
        repository.save(activity);
    }

    public List<ActivityLog> getRecentActivities() {
        return repository.findTop10ByOrderByTimestampDesc();
    }
}
