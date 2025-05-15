package com.example.supervision.controllers;

import com.example.supervision.Kafka.KafkaProducerService;
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
    private final KafkaProducerService kafkaProducerService ;

    @Autowired
    public ModerateurController(SuperviseurService superviseurService,
                              CourseRepository courseRepository ,
                              KafkaProducerService kafkaProducerService ) {
        this.superviseurService = superviseurService;
        this.courseRepository = courseRepository;
        this.kafkaProducerService= kafkaProducerService ;
    }

    @PutMapping("/{courseId}/approve")
    public ResponseEntity<Cours> approveCourse(@PathVariable UUID courseId , String status) {
          Cours cours = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cours non trouvé"));
    
          System.out.println("Tentative d'approbation du cours avec l'ID: " + courseId);
    
          cours.setStatus(CourseStatus.APPROVED);
          Cours savedCourse = courseRepository.save(cours);
    
          System.out.println("Cours approuvé avec succès: " + savedCourse.getId());

          kafkaProducerService.sendCoursStatus( courseId ,"APPROVED");

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

        kafkaProducerService.sendCoursStatus( courseId ,"REJECTED");

        return ResponseEntity.ok(savedCourse);
    }

    @PostMapping
    public ResponseEntity<User> addModerateur(
            @RequestBody User moderateurDTO,
            @RequestHeader("User-Type") String userType) {

        User createdModerateur = superviseurService.createModerateur(moderateurDTO, userType);
        return ResponseEntity.ok(createdModerateur);
    }

    @DeleteMapping("/{moderatorId}")
    public ResponseEntity<Void> deleteModerator(@PathVariable Long moderatorId) {
    superviseurService.deleteModerator(moderatorId);
    return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllModerateurs() {
        List<User> moderateurs = superviseurService.getAllModerateurs();
        return ResponseEntity.ok(moderateurs);
    }
}