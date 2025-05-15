package com.example.supervision.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.supervision.classes.Apprenant;

@Repository
public interface ApprenantRepository extends JpaRepository<Apprenant, Long> {
}