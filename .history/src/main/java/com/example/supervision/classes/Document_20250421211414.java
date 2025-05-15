package com.example.supervision.classes;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String fileUrl;
    private String documentType; // certificat wela experiences cv etc 
    
    @ManyToOne
   @JoinColumn(name = "formateur_id", foreignKey = @ForeignKey(name = "fk_document_formateur"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Formateur formateur;
    
    // Constructeurs
    public Document() {}
    
    public Document(String title, String fileUrl, String documentType) {
        this.title = title;
        this.fileUrl = fileUrl;
        this.documentType = documentType;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public String getDocumentType() {
        return documentType;
    }
    
    public Formateur getFormateur() {
        return formateur;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }
    
    public void setFormateur(Formateur formateur) {
        this.formateur = formateur;
    }
}