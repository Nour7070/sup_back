package com.example.supervision.services;

import com.example.supervision.classes.User;
import com.example.supervision.repositories.UserRepository;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> filterUsersByType(List<User> users, String userType) {
        return users.stream()
                .filter(user -> user.getUserType().equals(userType))
                .collect(Collectors.toList());
    }

    public List<User> getUsersFromDatabase(String userType) {
        try {
            return userRepository.findByUserType(userType);
        } catch (Exception e) {
            System.err.println("Error fetching users from database: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public long countStudentsSubscribedToFormateur(Long formateurId) {
        return userRepository.countBySubscribedFormateurId(formateurId);
    }

    public long getNombreApprenantsAbonnes(Long formateurId) {
        return formateurRepository.countApprenantsAbonnes(formateurId);
    }
}
