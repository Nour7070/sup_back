package com.example.supervision.controllers;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private StatsService statsService;

    @GetMapping("/formateurs/{id}")
    public ResponseEntity<Map<String, Object>> getFormateurStats(@PathVariable Long id) {
        return ResponseEntity.ok(statsService.getFormateurStats(id));
    }

    @GetMapping("/apprenants/{id}")
    public ResponseEntity<Map<String, Object>> getApprenantStats(@PathVariable Long id) {
        return ResponseEntity.ok(statsService.getApprenantStats(id));
    }
}