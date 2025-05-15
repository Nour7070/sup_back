package com.example.supervision.repositories;

import java.util.Optional;

import com.example.supervision.classes.Apprenant;

public class ApprenantRepository extends JpaRepository <Apprenant, Long> {

    public Optional<Apprenant> findById(Long id) {
        return null;
    }
}
