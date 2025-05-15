package com.example.supervision.controllers;


import com.example.supervision.services.SuperviseurService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final SuperviseurService superviseurService;

    @Autowired
    public DashboardController(
        SuperviseurService superviseurService) {
        this.superviseurService = superviseurService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userTypes", superviseurService.getUserTypeStats());
        stats.put("formateurs", superviseurService.getFormateurCount());
        stats.put("apprenants", superviseurService.getApprenantCount());
        stats.put("moderateurs", superviseurService.getModerateurCount());
        stats.put("averageCoursesPerTrainer", superviseurService.getAverageCoursesPerTrainer());

        return ResponseEntity.ok(stats);
    }
    
     @DeleteMapping("/formateurs/{id}")
     public ResponseEntity<?> deleteFormateur(@PathVariable Long id) {
         try {
             boolean deleted = superviseurService.deleteFormateur(id);
             if (deleted) {
                 return ResponseEntity.ok().body(Map.of("message", "Formateur supprimé avec succès"));
             } else {
                 return ResponseEntity.notFound().build();
             }
         } catch (Exception e) {
             return ResponseEntity.internalServerError()
                     .body(Map.of("error", "Erreur lors de la suppression du formateur"));
         }
     }
 
     // Endpoint pour supprimer plusieurs formateurs
     @DeleteMapping("/formateurs/batch")
     public ResponseEntity<?> deleteFormateursBatch(@RequestBody List<Long> ids) {
         try {
             int deletedCount = superviseurService.deleteFormateursBatch(ids);
             return ResponseEntity.ok()
                     .body(Map.of("message", deletedCount + " formateurs supprimés avec succès"));
         } catch (Exception e) {
             return ResponseEntity.internalServerError()
                     .body(Map.of("error", "Erreur lors de la suppression des formateurs"));
         }
     }
 
     // Endpoint pour supprimer un cours
     @DeleteMapping("/cours/{id}")
     public ResponseEntity<?> deleteCours(@PathVariable UUID id) {
         try {
             boolean deleted = superviseurService.deleteCours(id);
             if (deleted) {
                 return ResponseEntity.ok().body(Map.of("message", "Cours supprimé avec succès"));
             } else {
                 return ResponseEntity.notFound().build();
             }
         } catch (Exception e) {
             return ResponseEntity.internalServerError()
                     .body(Map.of("error", "Erreur lors de la suppression du cours"));
         }
     }
 
     // Endpoint pour supprimer plusieurs cours
     @DeleteMapping("/cours/batch")
     public ResponseEntity<?> deleteCoursBatch(@RequestBody List<UUID> ids) {
         try {
             int deletedCount = superviseurService.deleteCoursBatch(ids);
             return ResponseEntity.ok()
                     .body(Map.of("message", deletedCount + " cours supprimés avec succès"));
         } catch (Exception e) {
             return ResponseEntity.internalServerError()
                     .body(Map.of("error", "Erreur lors de la suppression des cours"));
         }
     }
}
