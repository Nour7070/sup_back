package com.example.supervision.services;

import com.example.supervision.classes.Apprenant;
import com.example.supervision.classes.Document;
import com.example.supervision.classes.User;
import com.example.supervision.repositories.ApprenantRepository;
import com.example.supervision.repositories.DocumentRepository;
import com.example.supervision.repositories.FormateurRepository;
import com.example.supervision.repositories.UserRepository;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository ;
    private final SupervisionFileService fileService ;
    private final ApprenantRepository apprenantRepository ;

    public UserService(UserRepository userRepository ,
    FormateurRepository formateurRepository ,
    DocumentRepository documentRepository ,
    SupervisionFileService fileService ,
    ApprenantRepository apprenantRepository) {
        this.userRepository = userRepository;
        this.formateurRepository = formateurRepository;
        this.documentRepository=documentRepository;
        this.fileService = fileService ;
        this.apprenantRepository =apprenantRepository ;
    }

    public List<User> filterUsersByType(List<User> users, String userType) {
        return users.stream()
                .filter(user -> user.getUserType().equals(userType))
                .collect(Collectors.toList());
    }
    public List<User> getUsersFromDatabase(String userType) {
        System.out.println("Searching for user type: " + userType);
        
        try {
            if ("APPRENANT".equalsIgnoreCase(userType)) {
                List<Apprenant> apprenants = apprenantRepository.findAllApprenantsByNativeQuery();
                System.out.println("Found apprenants: " + apprenants.size());
                return new ArrayList<User>(apprenants);
            } else {
                return userRepository.findByUserType(userType);
            }
        } catch (Exception e) {
            System.err.println("Erreur: " + e.getMessage());
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

   /* public long getNombreApprenantsAbonnes(Long formateurId) {
        return formateurRepository.countApprenantsAbonnes(formateurId);
    }*/

    /*@Transactional
    public Formateur registerFormateurWithDocuments(Formateur formateur, 
                                                 List<MultipartFile> certificats, 
                                                 List<MultipartFile> experiences) throws IOException {
        
        formateur.setStatus(UserStatus.PENDING);
        formateur.setCoursPublies(0L);
        formateur.setCoursEnAttente(0L);
        formateur.setEtudiantsAbonnes(0L);
    
        Formateur savedFormateur = formateurRepository.saveAndFlush(formateur);
        
        List<Document> documents = new ArrayList<>();
        
        if (certificats != null) {
            for (MultipartFile file : certificats) {
                Document doc = createDocument(file, "CERTIFICAT", savedFormateur);
                documents.add(documentRepository.save(doc));
            }
        }
        
        if (experiences != null) {
            for (MultipartFile file : experiences) {
                Document doc = createDocument(file, "EXPERIENCE", savedFormateur);
                documents.add(documentRepository.save(doc));
            }
        }
        
        savedFormateur.setDocuments(documents);
        return formateurRepository.save(savedFormateur);
    }*/

    
    public List<Document> getFormateurDocuments(Long formateurId) {
        return documentRepository.findByFormateur_Id(formateurId);
    }
    
    public List<Document> getFormateurDocumentsByType(Long formateurId, String documentType) {
        return documentRepository.findByFormateurIdAndDocumentType(formateurId, documentType);
    }
    

}
