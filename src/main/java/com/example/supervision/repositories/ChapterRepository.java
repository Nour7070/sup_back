package com.example.supervision.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.supervision.classes.Chapter;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, UUID> {
}