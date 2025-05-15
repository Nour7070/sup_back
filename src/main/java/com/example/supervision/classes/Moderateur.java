package com.example.supervision.classes;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MODERATEUR")
public class Moderateur extends User {
    
    private Long actionsCount;  
    
    public Long getActionsCount() {
        return actionsCount;
    }

    public void setActionsCount(Long actionsCount) {
        this.actionsCount = actionsCount;
    }
}