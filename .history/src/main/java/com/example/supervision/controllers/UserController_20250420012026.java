package com.example.supervision.controllers;

import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.User;
import com.example.supervision.services.CourseService;
import com.example.supervision.services.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CourseService courseService ;

    public UserController (UserService userService ,
    CourseService courseService){
        this.userService= userService ;
        this.courseService =courseService;
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

    @GetMapping("/db/{userType}")
    public ResponseEntity<List<User>> getUsersDirectFromDb(@PathVariable String userType) {
        List<User> users = userService.getUsersFromDatabase(userType);
        return ResponseEntity.ok(users);
    }
    @GetMapping("/formateurs/{id}/stats")
public ResponseEntity<Formateur> getFormateurStats(@PathVariable Long id) {
    // 1. Vérifier que l'utilisateur existe et est un formateur
    Formateur formateur = (Formateur) userService.getUserById(id)
            .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));
    
    if (!"FORMATEUR".equals(formateur.getUserType())) {
        throw new RuntimeException("L'utilisateur n'est pas un formateur");
    }

    // 2. Récupérer les statistiques
    Formateur stats = new Formateur();
    
    // Infos de base
    stats.setNom(formateur.getNom());
    stats.setPrenom(formateur.getPrenom());
    stats.setEmail(formateur.getEmail());
    stats.setPhoto(formateur.getPhoto());
    stats.setDateInscription(formateur.getDateInscription());
    
    // Statistiques métier
    stats.setCoursPublies(courseService.countCoursesByFormateurAndStatus(id, CourseStatus.APPROVED));
    stats.setCoursEnAttente(courseService.countCoursesByFormateurAndStatus(id, CourseStatus.PENDING));
    stats.setEtudiantsAbonnes(userService.countStudentsSubscribedToFormateur(id));

    return ResponseEntity.ok(stats);
}
}
