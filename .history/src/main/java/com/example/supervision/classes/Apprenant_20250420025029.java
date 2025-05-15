package com.example.supervision.classes;

import java.util.Set;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;

@Entity
@DiscriminatorValue("APPRENANT")
public class Apprenant extends User {
    
    @ManyToMany(mappedBy = "apprenantsAbonnes")
    private Set<Formateur> formateursSuivis;

    private Long coursSuivis;
    private Double tauxCompletion;
    

    // Getters et setters
    public Set<Formateur> getFormateursSuivis() {
        return formateursSuivis;
    }

    public Long getCoursSuivis() {
        return coursSuivis != null ? coursSuivis : 0L;
    }

    public void setCoursSuivis(Long coursSuivis) {
        this.coursSuivis = coursSuivis;
    }


    public Double getTauxCompletion() {
        return tauxCompletion != null ? tauxCompletion : 0.0;
    }

    public void setTauxCompletion(Double tauxCompletion) {
        this.tauxCompletion = tauxCompletion;
    }
    
}