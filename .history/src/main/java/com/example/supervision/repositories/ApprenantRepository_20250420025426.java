package com.example.supervision.repositories;

import java.util.Optional;

import com.example.supervision.classes.Apprenant;
import com.example.supervision.classes.Formateur;

public class ApprenantRepository extends JpaRepository <Appr, Long> {

    public Optional<Apprenant> findById(Long id) {
        return null;
    }
}
