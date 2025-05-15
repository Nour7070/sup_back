package com.example.supervision.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.supervision.classes.Apprenant;

@Repository
public interface ApprenantRepository extends JpaRepository<Apprenant, Long> {
    // Pas besoin d'implémenter findById, c'est déjà fourni par JpaRepository
    // Vous pouvez ajouter d'autres méthodes personnalisées si nécessaire
}