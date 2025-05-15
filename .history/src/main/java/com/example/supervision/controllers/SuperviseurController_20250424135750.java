package com.example.supervision.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.supervision.classes.Cours;
import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.User;
import com.example.supervision.repositories.FormateurRepository;
import com.example.supervision.services.CourseService;
import com.example.supervision.services.SuperviseurService;

@RestController
@RequestMapping("/supervision/formateurs")
public class SuperviseurController {

    private final SuperviseurService superviseurService;

    public SuperviseurController(SuperviseurService superviseurService ,
    FormateurRepository formateurRepository ,
    CourseService courseService) {
        this.superviseurService = superviseurService;
    }

    @GetMapping("/pending")
    public List<User> getPendingFormateurs() {
        return superviseurService.getPendingFormateurs();
    }

    /*@PutMapping("{email}/approve")
    public ResponseEntity<?> approveFormateurByEmail(@PathVariable String email) {
        Formateur formateur = superviseurService.approveFormateur(email);
        return ResponseEntity.ok(formateur);
    }

    @PutMapping("{email}/reject")
    public User rejectFormateurByEmail(@PathVariable String email) {
        return superviseurService.rejectFormateur(email);
    }
    //hedi cours en attente alabali ca porte à confusion ici
    @GetMapping("/attente")
    public ResponseEntity<List<Cours>> getPendingCourses() {
    List<Cours> pendingCourses = superviseurService.getPendingCourses();
    return ResponseEntity.ok(pendingCourses);
    }

}
