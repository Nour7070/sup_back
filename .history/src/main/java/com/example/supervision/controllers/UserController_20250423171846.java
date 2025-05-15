package com.example.supervision.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.supervision.classes.Apprenant;
import com.example.supervision.classes.Document;
import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.UserStatus;
import com.example.supervision.repositories.ApprenantRepository;
import com.example.supervision.repositories.FormateurRepository;
import com.example.supervision.services.ActivityLogService;
import com.example.supervision.services.CourseService;
import com.example.supervision.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CourseService courseService ;
    private final ApprenantRepository apprenantRepository ;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ActivityLogService activityLogService ;
    private final FormateurRepository formateurRepository ;

    public UserController (UserService userService ,
    CourseService courseService , 
    ApprenantRepository apprenantRepository ,
    ObjectMapper objectMapper ,
    JdbcTemplate jdbcTemplate ,
    ActivityLogService activityLogService ,
    FormateurRepository formateurRepository){
        this.userService= userService ;
        this.courseService =courseService;
        this.apprenantRepository =apprenantRepository ;
        this.objectMapper =objectMapper ;
        this.jdbcTemplate = jdbcTemplate;
        this.activityLogService = activityLogService; 
        this.formateurRepository =formateurRepository ;
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

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Formateur> registerFormateur(
        @RequestPart("formateur") String formateurJson,
        @RequestPart(value = "certificats", required = false) List<MultipartFile> certificats,
        @RequestPart(value = "experiences", required = false) List<MultipartFile> experiences) throws IOException {
        
        Formateur formateur = objectMapper.readValue(formateurJson, Formateur.class);
        Formateur registeredFormateur = userService.registerFormateurWithDocuments(formateur, certificats, experiences);
        String description = String.format("Trainer %s created and pending approval.", formateur.getNom());
        activityLogService.log("Nouveau formateur inscrit", description, formateur.getId());

        return ResponseEntity.ok(registeredFormateur);
    }
    
    @PostMapping(value = "/{formateurId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Formateur> addDocuments(
        @PathVariable Long formateurId,
        @RequestPart(value = "certificats", required = false) List<MultipartFile> certificats,
        @RequestPart(value = "experiences", required = false) List<MultipartFile> experiences) throws IOException {
        
        Formateur updatedFormateur = userService.addDocumentsToFormateur(formateurId, certificats, experiences);
        return ResponseEntity.ok(updatedFormateur);
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

    //temporaire heda
    @PostMapping("/register-apprenant")
public ResponseEntity<Apprenant> createApprenantInDb(@RequestBody Apprenant apprenant) {
    
    Apprenant savedApprenant = apprenantRepository.save(apprenant);
    
    return ResponseEntity.ok(savedApprenant);
}

}
