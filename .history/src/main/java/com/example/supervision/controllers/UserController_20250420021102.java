package com.example.supervision.controllers;

import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.User;
import com.example.supervision.repositories.UserRepository;
import com.example.supervision.services.CourseService;
import com.example.supervision.services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    public UserController (UserService userService ,
    CourseService courseService , 
    UserRepository userRepository){
        this.userService= userService ;
        this.courseService =courseService;
        this.userRepository =userRepository;
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
        
        // Transformation pour éviter les propriétés null
        List<Map<String, Object>> response = users.stream()
            .map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("email", user.getEmail());
                userMap.put("nom", user.getNom());
                userMap.put("prenom", user.getPrenom());
                userMap.put("username", user.getUsername());
                userMap.put("userType", user.getUserType());
                // Ajoutez d'autres propriétés communes
                return userMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
               .body(Map.of("error", "Erreur de récupération des utilisateurs"));
    }
    
    @GetMapping("/formateurs/{id}/stats")
    public ResponseEntity<Formateur> getFormateurStats(@PathVariable Long id) {
        Formateur formateur = userRepository.findFormateurById(id)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));
        
        // Récupération des statistiques
        formateur.setCoursPublies(courseService.countCoursesByFormateurAndStatus(id, CourseStatus.APPROVED));
        formateur.setCoursEnAttente(courseService.countCoursesByFormateurAndStatus(id, CourseStatus.PENDING));
        formateur.setEtudiantsAbonnes(userService.countStudentsSubscribedToFormateur(id));
    
        return ResponseEntity.ok(formateur);
    }
}
