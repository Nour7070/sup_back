package com.example.supervision.repositories;

import java.util.List;

import com.example.supervision.classes.Document;

public interface DocumentRepository extends JpaRepository<Document, Long>  {

    public void saveAll(List<Document> documents) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveAll'");
    }

    public void deleteById(Long documentId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }

    public List<Document> findByFormateurId(Long formateurId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByFormateurId'");
    }

    public List<Document> findByFormateurIdAndDocumentType(Long formateurId, String documentType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByFormateurIdAndDocumentType'");
    }
    
}
