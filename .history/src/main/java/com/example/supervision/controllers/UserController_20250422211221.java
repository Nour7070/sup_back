package com.example.supervision.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.common.errors.ResourceNotFoundException;
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
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.Document;
import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.User;
import com.example.supervision.classes.UserStatus;
import com.example.supervision.repositories.ApprenantRepository;
import com.example.supervision.repositories.DocumentRepository;
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
    private final CourseService courseService ;
    private final UserRepository userRepository ;
    private final ApprenantRepository apprenantRepository ;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final DocumentRepository documentRepository ;
    private final ActivityLogService activityLogService ;
    private final FormateurRepository formateurRepository ;

    public UserController (UserService userService ,
    CourseService courseService , 
    UserRepository userRepository ,
    ApprenantRepository apprenantRepository ,
    ObjectMapper objectMapper ,
    JdbcTemplate jdbcTemplate ,
    DocumentRepository documentRepository ,
    ActivityLogService activityLogService ,
    FormateurRepository formateurRepository){
        this.userService= userService ;
        this.courseService =courseService;
        this.userRepository =userRepository;
        this.apprenantRepository =apprenantRepository ;
        this.objectMapper =objectMapper ;
        this.jdbcTemplate = jdbcTemplate;
        this.documentRepository = documentRepository; 
        this.activityLogService = activityLogService; 
        this.formateurRepository =formateurRepository ;
    }

    private List<User> users = new ArrayList<>();

    @GetMapping("/type/{userType}")
    public ResponseEntity<List<User>> getUsersByType(@PathVariable String userType) {
        if (users == null) {
            users = new ArrayList<>();
        }
        List<User> filteredUsers = userService.filterUsersByType(users, userType);
        return ResponseEntity.ok(filteredUsers);
    }

    @GetMapping("/db/formateurs/approved")
    public List<Formateur> getApprovedFormateurs() {
        return formateurRepository.findByStatus(UserStatus.APPROVED);
}

    @GetMapping("/db/{userType}")
public ResponseEntity<?> getUsersDirectFromDb(@PathVariable String userType) {
    try {
        if ("APPRENANT".equalsIgnoreCase(userType)) {
            List<Map<String, Object>> apprenants = jdbcTemplate.queryForList(
                "SELECT * FROM users WHERE user_type = 'APPRENANT'"
            );
            return ResponseEntity.ok(apprenants);
        } else {
            List<User> users = userService.getUsersFromDatabase(userType);
            
            List<Map<String, Object>> response = users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("email", user.getEmail());
                    userMap.put("nom", user.getNom());
                    userMap.put("prenom", user.getPrenom());
                    userMap.put("username", user.getUsername());
                    userMap.put("userType", user.getUserType());
                    return userMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        }
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur de récupération des utilisateurs: " + e.getMessage()));
    }
   }

    @GetMapping("/apprenants/{id}/stats")
    public Map<String, Object> getApprenantStats(@PathVariable Long id) {
        Apprenant apprenant = apprenantRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Apprenant not found"));
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("coursSuivis", apprenant.getCoursSuivis());
        stats.put("tauxCompletion", apprenant.getTauxCompletion());
        stats.put("formateursSuivis", apprenant.getFormateursSuivis().size());
        
        return stats;
    }

    @GetMapping("/apprenants/{id}/courses")
    public List<Cours> getApprenantCourses(@PathVariable Long id) {
        return courseService.getCoursesForApprenant(id);
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
