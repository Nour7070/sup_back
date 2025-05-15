package com.example.supervision.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.supervision.classes.Apprenant;
import com.example.supervision.classes.Document;
import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.User;
import com.example.supervision.classes.UserStatus;
import com.example.supervision.repositories.ApprenantRepository;
import com.example.supervision.repositories.FormateurRepository;
import com.example.supervision.repositories.UserRepository;
import com.example.supervision.services.ActivityLogService;
import com.example.supervision.services.CourseService;
import com.example.supervision.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JdbcTemplate jdbcTemplate;
    private final FormateurRepository formateurRepository ;
    private final UserRepository userRepository ;

    public UserController (UserService userService ,
    CourseService courseService , 
    ApprenantRepository apprenantRepository ,
    ObjectMapper objectMapper ,
    JdbcTemplate jdbcTemplate ,
    ActivityLogService activityLogService ,
    FormateurRepository formateurRepository ,
    UserRepository userRepository){
        this.userService= userService ;
        this.courseService =courseService;
        this.apprenantRepository =apprenantRepository ;
        this.objectMapper =objectMapper ;
        this.jdbcTemplate = jdbcTemplate;
        this.activityLogService = activityLogService; 
        this.formateurRepository =formateurRepository ;
        this.userRepository = userRepository ;
    }


    @GetMapping("/db/formateurs/approved")
    public List<Formateur> getApprovedFormateurs() {
        return formateurRepository.findByStatus(UserStatus.APPROVED);
}

   
    @GetMapping("/db/apprenants")
    public ResponseEntity<?> getApprenantsFromDb() {
     try {
        List<Map<String, Object>> apprenants = jdbcTemplate.queryForList(
            "SELECT * FROM users WHERE user_type = 'APPRENANT'"
        );
        return ResponseEntity.ok(apprenants);
     } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Erreur de récupération des apprenants: " + e.getMessage()));
    }
}

    @GetMapping("/{formateurId}/documents")
    public ResponseEntity<List<Document>> getDocuments(@PathVariable Long formateurId) {
        return ResponseEntity.ok(userService.getFormateurDocuments(formateurId));
    }
    
    @GetMapping("/{formateurId}/documents/{type}")
    public ResponseEntity<List<Document>> getDocumentsByType(
        @PathVariable Long formateurId,
        @PathVariable String type) {
        
        return ResponseEntity.ok(userService.getFormateurDocumentsByType(formateurId, type));
    }
}
