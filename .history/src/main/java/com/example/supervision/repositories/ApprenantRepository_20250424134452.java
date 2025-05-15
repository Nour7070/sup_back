package com.example.supervision.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.supervision.classes.Apprenant;

@Repository
public interface ApprenantRepository extends JpaRepository<Apprenant, Long> {
    @Query(value = "SELECT * FROM users WHERE user_type = 'APPRENANT'", nativeQuery = true)
    List<Apprenant> findAllApprenantsByNativeQuery();

    Optional<Apprenant> findByEmail(String email);
    
    Optional<Apprenant> findByUsername(String username);
}