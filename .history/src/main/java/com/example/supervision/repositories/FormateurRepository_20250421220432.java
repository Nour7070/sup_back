package com.example.supervision.repositories;

import com.example.supervision.classes.Formateur;

import feign.Param;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormateurRepository extends JpaRepository<Formateur, Long> {

    Optional<Formateur> findByEmail(String email);
    @Query("SELECT SIZE(f.apprenantsAbonnes) FROM Formateur f WHERE f.id = :formateurId")
    long countApprenantsAbonnes(@Param("formateurId") Long formateurId);

    List<Formateur> findByStatus(String status);
}