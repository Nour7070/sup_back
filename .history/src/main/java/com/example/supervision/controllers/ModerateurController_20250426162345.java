package com.example.supervision.controllers;

import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.User;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.services.SuperviseurService;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/moderateurs")
public class ModerateurController {

    private final SuperviseurService superviseurService;
    private final CourseRepository courseRepository;

    @Autowired
    public ModerateurController(SuperviseurService superviseurService,
                              CourseRepository courseRepository) {
        this.superviseurService = superviseurService;
        this.courseRepository = courseRepository;
    }

    @PutMapping("/{courseId}/approve")
    public ResponseEntity<Cours> approveCourse(@PathVariable UUID courseId) {
        Cours cours = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cours non trouvé"));

        cours.setStatus(CourseStatus.APPROVED);
        Cours savedCourse = courseRepository.save(cours);

        //validationResponseProducer.sendValidationResponse(courseId, true, "Cours approuvé");

        String description = String.format("Moderator %s signed up .", apprenant.getNom());
        activityLogService.log("MOD_APPROVE_COURSE", description, apprenant.getId());
        return ResponseEntity.ok(savedCourse);
    }

    @PutMapping("/{courseId}/reject")
    public ResponseEntity<Cours> rejectCourse(
            @PathVariable UUID courseId,
            @RequestParam(required = false) String rejectionReason) {

        Cours cours = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cours non trouvé"));

        cours.setStatus(CourseStatus.REJECTED);
        Cours savedCourse = courseRepository.save(cours);

        //validationResponseProducer.sendValidationResponse(courseId, false,
                //rejectionReason != null ? rejectionReason : "Cours rejeté");

        return ResponseEntity.ok(savedCourse);
    }

    @PostMapping
    public ResponseEntity<User> addModerateur(
            @RequestBody User moderateurDTO,
            @RequestHeader("User-Type") String userType) {

        User createdModerateur = superviseurService.createModerateur(moderateurDTO, userType);
        return ResponseEntity.ok(createdModerateur);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllModerateurs() {
        List<User> moderateurs = superviseurService.getAllModerateurs();
        return ResponseEntity.ok(moderateurs);
    }
}