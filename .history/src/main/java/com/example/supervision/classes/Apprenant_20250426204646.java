package com.example.supervision.classes;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
@DiscriminatorValue("APPRENANT")
public class Apprenant extends User {
    
    @ManyToMany(mappedBy = "apprenantsAbonnes")
    private Set<Formateur> formateursSuivis;

    private Double tauxCompletion;
    /*@ManyToMany
@JoinTable(
    name = "apprenant_cours",
    joinColumns = @JoinColumn(name = "apprenant_id"),
    inverseJoinColumns = @JoinColumn(name = "cours_id")
)
private Set<Cours> coursSuivis = new HashSet<>();*/

public Set<Cours> getCoursSuivis() {
    return coursSuivis != null ? coursSuivis : new HashSet<>();
}

    public Set<Formateur> getFormateursSuivis() {
        return formateursSuivis;
    }

    public Object getCoursSuivis() {
        return coursSuivis != null ? coursSuivis : 0L;
    }

    public void setCoursSuivis(Set<Cours> coursSuivis) {
        this.coursSuivis = coursSuivis;
    }


    public Double getTauxCompletion() {
        return tauxCompletion != null ? tauxCompletion : 0.0;
    }

    public void setTauxCompletion(Double tauxCompletion) {
        this.tauxCompletion = tauxCompletion;
    }
    
}