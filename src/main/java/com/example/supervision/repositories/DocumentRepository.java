package com.example.supervision.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.supervision.classes.Document;

public interface DocumentRepository extends JpaRepository<Document, Long>  {

    List<Document> findByFormateur_Id(Long formateurId);
    List<Document> findByFormateurIdAndDocumentType(Long formateurId, String documentType);
    
}
