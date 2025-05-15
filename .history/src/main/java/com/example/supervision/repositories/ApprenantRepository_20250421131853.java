package com.example.supervision.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.supervision.classes.Apprenant;

@Repository
public interface ApprenantRepository extends JpaRepository<Apprenant, Long> {
     // Ajoutez cette méthode si elle n'existe pas
    List<Apprenant> findByUserType(String userType);
    
    // Ou cette version plus générique
    @Query("SELECT a FROM Apprenant a WHERE a.userType = :userType")
    List<Apprenant> findByType(@Param("userType") String userType);
}