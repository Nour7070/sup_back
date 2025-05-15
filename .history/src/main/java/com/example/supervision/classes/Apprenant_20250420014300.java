package com.example.supervision.classes;

import java.util.Set;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;

@Entity
@DiscriminatorValue("APPRENANT")
public class Apprenant extends User {
    
/*@ManyToMany(mappedBy = "apprenantsAbonnes")
    private Set<Formateur> formateursSuivis;

    public Set<Formateur> getFormateursSuivis() {
        return formateursSuivis;
    }
}