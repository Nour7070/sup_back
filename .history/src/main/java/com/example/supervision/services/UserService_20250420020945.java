package com.example.supervision.services;

import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.User;
import com.example.supervision.repositories.FormateurRepository;
import com.example.supervision.repositories.UserRepository;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FormateurRepository formateurRepository;

    public UserService(UserRepository userRepository ,
    FormateurRepository formateurRepository) {
        this.userRepository = userRepository;
        this.formateurRepository = formateurRepository;
    }

    public List<User> filterUsersByType(List<User> users, String userType) {
        return users.stream()
                .filter(user -> user.getUserType().equals(userType))
                .collect(Collectors.toList());
    }

    /*public List<User> getUsersFromDatabase(String userType) {
        try {
            return userRepository.findByUserType(userType);
        } catch (Exception e) {
            System.err.println("Error fetching users from database: " + e.getMessage());
            return Collections.emptyList();
        }
    }*/
    public List<User> getUsersFromDatabase(String userType) {
    try {
        List<User> users = userRepository.findByUserType(userType);
        
        users.forEach(user -> {
            if (user instanceof Formateur) {
                Formateur f = (Formateur) user;
                if (f.getCoursEnAttente() == null) f.setCoursEnAttente(0L);
                if (f.getCoursPublies() == null) f.setCoursPublies(0L);
                if (f.getEtudiantsAbonnes() == null) f.setEtudiantsAbonnes(0L);
            }
        });
        
        return users;
    } catch (Exception e) {
        e.printStackTrace();
        return Collections.emptyList();
    }
}

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public long countStudentsSubscribedToFormateur(Long formateurId) {
        return userRepository.countApprenantsByFormateurId(formateurId);
    }

    public long getNombreApprenantsAbonnes(Long formateurId) {
        return formateurRepository.countApprenantsAbonnes(formateurId);
    }
}
