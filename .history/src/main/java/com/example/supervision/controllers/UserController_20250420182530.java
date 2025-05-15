package com.example.supervision.controllers;

import com.example.supervision.classes.Apprenant;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.Document;
import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.User;
import com.example.supervision.repositories.ApprenantRepository;
import com.example.supervision.repositories.UserRepository;
import com.example.supervision.services.CourseService;
import com.example.supervision.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CourseService courseService ;
    private final UserRepository userRepository ;
    private final ApprenantRepository apprenantRepository ;
    private final ObjectMapper objectMapper;


    public UserController (UserService userService ,
    CourseService courseService , 
    UserRepository userRepository ,
    ApprenantRepository apprenantRepository ,
    ObjectMapper objectMapper){
        this.userService= userService ;
        this.courseService =courseService;
        this.userRepository =userRepository;
        this.apprenantRepository =apprenantRepository ;
        this.objectMapper =objectMapper ;
    }

    private List<User> users = new ArrayList<>();

    @GetMapping("/{userType}")
    public ResponseEntity<List<User>> getUsersByType(@PathVariable String userType) {
        if (users == null) {
            users = new ArrayList<>();
        }
        List<User> filteredUsers = userService.filterUsersByType(users, userType);
        return ResponseEntity.ok(filteredUsers);
    }

    /*@GetMapping("/db/{userType}")
    public ResponseEntity<List<User>> getUsersDirectFromDb(@PathVariable String userType) {
        List<User> users = userService.getUsersFromDatabase(userType);
        return ResponseEntity.ok(users);
    }*/

    @GetMapping("/db/{userType}")
public ResponseEntity<?> getUsersDirectFromDb(@PathVariable String userType) {
    try {
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
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
               .body(Map.of("error", "Erreur de récupération des utilisateurs"));
    }
}
    @GetMapping("/formateurs/{id}/stats")
    public ResponseEntity<Formateur> getFormateurStats(@PathVariable Long id) {
        Formateur formateur = userRepository.findFormateurById(id)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));
        
        formateur.setCoursPublies(courseService.countCoursesByFormateurAndStatus(id, CourseStatus.APPROVED));
        formateur.setCoursEnAttente(courseService.countCoursesByFormateurAndStatus(id, CourseStatus.PENDING));
        formateur.setEtudiantsAbonnes(userService.countStudentsSubscribedToFormateur(id));
    
        return ResponseEntity.ok(formateur);
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
    
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId) {
        userService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
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
