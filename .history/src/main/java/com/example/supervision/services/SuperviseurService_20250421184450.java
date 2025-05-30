package com.example.supervision.services;

import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.Moderateur;
import com.example.supervision.classes.User;
import com.example.supervision.classes.UserStatus;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.repositories.FormateurRepository;
import com.example.supervision.repositories.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
@Service
public class SuperviseurService {

    private final UserRepository userRepository;
    private final FormateurRepository formateurRepository;
    private final CourseRepository courseRepository ;

    public SuperviseurService(UserRepository userRepository,
            FormateurRepository formateurRepository , 
            CourseRepository courseRepository  ){
        this.userRepository = userRepository;
        this.formateurRepository = formateurRepository;
        this.courseRepository = courseRepository ;

    }
    public User createModerateur(User moderateurDTO, String userType) {
        
        Moderateur moderateur = new Moderateur();
        moderateur.setNom(moderateurDTO.getNom());
        moderateur.setPrenom(moderateurDTO.getPrenom());
        moderateur.setUsername(moderateurDTO.getUsername());
        moderateur.setEmail(moderateurDTO.getEmail());
        moderateur.setPassword(moderateurDTO.getPassword());
        moderateur.setPhoneNumber(moderateurDTO.getPhoneNumber());
        moderateur.setPhoto(moderateurDTO.getPhoto());
        moderateur.setAddress(moderateurDTO.getAddress());
        moderateur.setValidated(moderateurDTO.isValidated());

        User savedModerator = userRepository.save(moderateur);

        Map<String, Object> ModerateurData = Map.of(
            "email", savedModerator.getEmail(),
            "prenom", savedModerator.getPrenom(),
            "nom", savedModerator.getNom(),
            "username", savedModerator.getUsername(),
            "phone" , savedModerator.getPhoneNumber(),
            "password", savedModerator.getPassword()
        );

        return savedModerator;
    }

  
    public long getFormateurCount() {
        return userRepository.countByUserType("FORMATEUR");
    }

    public long getApprenantCount() {
        return userRepository.countByUserType("APPRENANT");
    }

    public long getModerateurCount() {
        return userRepository.countByUserType("MODERATEUR");
    }

    public double getAverageCoursesPerTrainer() {
        long totalCourses = courseRepository.count(); 
        long totalTrainers = getFormateurCount(); 
    
        if (totalTrainers == 0) {
            return 0.0; // bech ma nqessmouch ala 
        }
        
        return (double) totalCourses / totalTrainers; 
    }
    
    public Map<String, Long> getUserTypeStats() {
        Map<String, Long> userStats = new HashMap<>();
        userStats.put("formateurs", getFormateurCount());
        userStats.put("apprenants", getApprenantCount());
        userStats.put("moderateurs", getModerateurCount());
        return userStats;
    }

   
    public Formateur approveFormateur(String email) {
        Formateur formateur = formateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé avec l'email: " + email));
    
        formateur.setStatus(UserStatus.APPROVED);
        Formateur savedFormateur = formateurRepository.save(formateur);
    
    
        return savedFormateur;
    }
    

    public User rejectFormateur(String email) {
        User formateur = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé avec l'email: " + email));
    
       
        if (!(formateur instanceof Formateur)) {
            throw new IllegalStateException("L'utilisateur n'est pas un formateur");
        }
    
        Formateur formateurToReject = (Formateur) formateur;
        formateurToReject.setStatus(UserStatus.REJECTED);
        userRepository.save(formateurToReject);  
    
        return formateurToReject;
    }


    public List<User> getPendingFormateurs() {
        return userRepository.findPendingFormateurs();
    }

    public List<User> getAllModerateurs() {
        return userRepository.findByUserType("MODERATEUR");
    }

    public Resource downloadFileFromUrl(String fileUrl) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Resource> response = restTemplate.getForEntity(fileUrl, Resource.class);
        
        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody(); 
        } else {
            throw new RuntimeException("Échec du téléchargement du fichier");
        }
    }
    public List<Cours> getPendingCourses() {
        return courseRepository.findByStatusWithChapters(CourseStatus.PENDING);
    
    }
  // hedou temporaire
public Formateur simulFormateur(Formateur formateur) {
    formateur.setStatus(UserStatus.PENDING); 
    return formateurRepository.save(formateur); 
}

@Transactional
public boolean deleteFormateur(Long id) {
    return formateurRepository.findById(id)
            .map(formateur -> {
                formateurRepository.delete(formateur);
                return true;
            })
            .orElse(false);
}


@Transactional
public int deleteFormateursBatch(List<Long> ids) {
    List<Formateur> toDelete = formateurRepository.findAllById(ids);
    formateurRepository.deleteAll(toDelete);
    return toDelete.size();
}

@Transactional
public boolean deleteCours(UUID id) {
    return courseRepository.findById(id)
            .map(cours -> {
                courseRepository.delete(cours);
                return true;
            })
            .orElse(false);
}

@Transactional
public int deleteCoursBatch(List<UUID> ids) {
    List<Cours> toDelete = courseRepository.findAllByIdIn(ids);
    courseRepository.deleteAll(toDelete);
    return toDelete.size();
}
}
