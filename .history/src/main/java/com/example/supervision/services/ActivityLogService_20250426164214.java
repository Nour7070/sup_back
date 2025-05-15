package com.example.supervision.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.supervision.classes.ActivityLog;
import com.example.supervision.repositories.ActivityLogRepository;

@Service
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public ActivityLogService(ActivityLogRepository repository) {
        this.repository = repository;
    }

    /*public void log(String type, String description, Long formateurId) {
        ActivityLog activity = new ActivityLog();
        activity.setType(type);
        activity.setDescription(description);
        activity.setTimestamp(LocalDateTime.now());
        activity.setFormateurId(formateurId); 
        repository.save(activity);
    }*/

    


    public List<ActivityLog> getRecentActivities() {
        return repository.findTop10ByOrderByTimestampDesc();
    }

    public List<ActivityLog> getModeratorActivities() {
        return repository.findByTypeInOrderByTimestampDesc(
            List.of("COURSE_APPROVAL", "COURSE_REJECTION", "TRAINER_APPROVAL", "TRAINER_REJECTION")
        );
    }
}
