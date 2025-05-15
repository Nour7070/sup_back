package com.example.supervision.controllers;


import com.example.supervision.services.SuperviseurService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final SuperviseurService superviseurService;

    @Autowired
    public DashboardController(
        SuperviseurService superviseurService) {
        this.superviseurService = superviseurService;
    }

    //hedou les stats du dashboard
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userTypes", superviseurService.getUserTypeStats());
        stats.put("formateurs", superviseurService.getFormateurCount());
        stats.put("apprenants", superviseurService.getApprenantCount());
        stats.put("moderateurs", superviseurService.getModerateurCount());
        stats.put("averageCoursesPerTrainer", superviseurService.getAverageCoursesPerTrainer());

        return ResponseEntity.ok(stats);
    }
    

    
}
