package com.example.supervision.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.supervision.classes.Apprenant;

public class ApprenantRepository extends JpaRepository <Apprenant, Long> {

    public Optional<Apprenant> findById(Long id) {
        return null;
    }
}
