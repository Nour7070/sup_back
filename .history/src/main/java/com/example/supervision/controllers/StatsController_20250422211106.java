package com.example.supervision.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.supervision.services.StatsService;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    //stats des formateurs dans user stat
    @GetMapping("/formateurs/{id}")
    public ResponseEntity<Map<String, Object>> getFormateurStats(@PathVariable Long id) {
        return ResponseEntity.ok(statsService.getFormateurStats(id));
    } 
}