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

    @GetMapping("/db/{userType}")
    public ResponseEntity<List<User>> getUsersDirectFromDb(@PathVariable String userType) {
        List<User> users = userService.getUsersFromDatabase(userType);
        return ResponseEntity.ok(users);
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
