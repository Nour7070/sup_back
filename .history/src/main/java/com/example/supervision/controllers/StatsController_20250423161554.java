package com.example.supervision.controllers;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.supervision.classes.CourseStatus;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.services.StatsService;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;
    @Autowired
    private CourseRepository courseRepository ;

    //stats des formateurs dans user stat
    @GetMapping("/formateurs/{id}")
    public ResponseEntity<Map<String, Object>> getFormateurStats(@PathVariable Long id) {
        return ResponseEntity.ok(statsService.getFormateurStats(id));
    } 

    /*@GetMapping("/formateurs")
    public ResponseEntity<Map<String, Object>> getFormateursStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        stats.put("totalFormateurs", statsService.getTotalFormateursCount());
        stats.put("courses", Map.of(
            "total", courseRepository.count(),
            "approved", courseRepository.countByStatus(CourseStatus.APPROVED),
            "pending", courseRepository.countByStatus(CourseStatus.PENDING)
        ));
        
        stats.put("averageCoursesPerTrainer", statsService.getAverageCoursesPerTrainer());
        
        return ResponseEntity.ok(stats);
    }*/
}