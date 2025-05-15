package com.example.supervision.controllers;

import com.example.supervision.Kafka.KafkaProducerService;
import com.example.supervision.classes.ActivityLog;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.Moderateur;
import com.example.supervision.classes.User;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.repositories.ModerateurRepository;
import com.example.supervision.services.ActivityLogService;
import com.example.supervision.services.SuperviseurService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final ActivityLogService activityLogService ;
    private final ModerateurRepository moderateurRepository ; 
    private final KafkaProducerService kafkaProducerService ;

    @Autowired
    public ModerateurController(SuperviseurService superviseurService,
                              CourseRepository courseRepository ,
                              ActivityLogService activityLogService , 
                              ModerateurRepository moderateurRepository ,
                              KafkaProducerService kafkaProducerService ) {
        this.superviseurService = superviseurService;
        this.courseRepository = courseRepository;
        this.activityLogService = activityLogService ;
        this.moderateurRepository = moderateurRepository ;
        this.kafkaProducerService= kafkaProducerService ;
    }

    /*@PutMapping("/{courseId}/approve")
    public ResponseEntity<Cours> approveCourse(@PathVariable UUID courseId) {
        Cours cours = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cours non trouvé"));

        cours.setStatus(CourseStatus.APPROVED);
        Cours savedCourse = courseRepository.save(cours);

        //validationResponseProducer.sendValidationResponse(courseId, true, "Cours approuvé");

        return ResponseEntity.ok(savedCourse);
    }*/

    @PutMapping("/{courseId}/approve")
public ResponseEntity<Cours> approveCourse(@PathVariable UUID courseId , CourseStatus ) {
    Cours cours = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cours non trouvé"));
    
    System.out.println("Tentative d'approbation du cours avec l'ID: " + courseId);
    
    cours.setStatus(CourseStatus.APPROVED);
    Cours savedCourse = courseRepository.save(cours);
    
    System.out.println("Cours approuvé avec succès: " + savedCourse.getId());

    kafkaProducerService.sendCoursStatus( courseId ,"APPROVED");

    return ResponseEntity.ok(savedCourse);
}


    /*@PutMapping("/{courseId}/approve")
    public ResponseEntity<Cours> approveCourse(
        @PathVariable UUID courseId,
        @AuthenticationPrincipal UserDetails userDetails) {
        
        // Récupérer l'utilisateur courant
        Moderateur moderateur = moderateurRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        
        Cours cours = courseRepository.findById(courseId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cours non trouvé"));
    
        cours.setStatus(CourseStatus.APPROVED);
        cours.setApprovedBy(moderateur.getId());
        Cours savedCourse = courseRepository.save(cours);

        CourseStatus status = CourseStatus.APPROVED;
        String message = "Le cours a été approuvé avec succès.";
       // validationProducer.sendValidationResult(courseId, status, message);
    
        String description = String.format("Le modérateur %s %s a approuvé le cours: %s", 
            moderateur.getPrenom(), moderateur.getNom(), cours.getTitre());
        
        activityLogService.log(
            "COURSE_APPROVAL", 
            description,
            cours.getFormateurId(), // Utilisez directement getFormateurId()
            moderateur.getId(),
            moderateur.getPrenom() + " " + moderateur.getNom()
        );
    
        return ResponseEntity.ok(savedCourse);
    }*/

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

    /*@PutMapping("/{courseId}/reject")
public ResponseEntity<Cours> rejectCourse(
    @PathVariable UUID courseId,
    @RequestParam(required = false) String rejectionReason,
    @AuthenticationPrincipal UserDetails userDetails) {

    Moderateur moderateur = moderateurRepository.findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
    
    Cours cours = courseRepository.findById(courseId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cours non trouvé"));

    cours.setStatus(CourseStatus.REJECTED);
    cours.setApprovedBy(moderateur.getId()); 
    Cours savedCourse = courseRepository.save(cours);

    CourseStatus status = CourseStatus.REJECTED;
    String message = rejectionReason != null ? rejectionReason : "Le cours a été rejeté sans raison spécifiée.";
   // validationProducer.sendValidationResult(courseId, status, message);

    String description = String.format("Le modérateur %s %s a rejeté le cours: %s", 
        moderateur.getPrenom(), moderateur.getNom(), cours.getTitre());
    
    activityLogService.log(
        "COURSE_REJECTION", 
        description,
        cours.getFormateurId(),
        moderateur.getId(),
        moderateur.getPrenom() + " " + moderateur.getNom()
    );

    return ResponseEntity.ok(savedCourse);
}*/

    @GetMapping("/moderator-actions")
public ResponseEntity<List<ActivityLog>> getModeratorActions(
    @AuthenticationPrincipal UserDetails userDetails) {
    
    // Vérifier si l'utilisateur est superviseur
    if (!userDetails.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("SUPERVISEUR"))) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
    
    return ResponseEntity.ok(activityLogService.getModeratorActivities());
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