package com.example.supervision.services;

import com.example.supervision.classes.Document;
import com.example.supervision.classes.Formateur;
import com.example.supervision.classes.User;
import com.example.supervision.classes.UserStatus;
import com.example.supervision.repositories.DocumentRepository;
import com.example.supervision.repositories.FormateurRepository;
import com.example.supervision.repositories.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FormateurRepository formateurRepository;
    private final DocumentRepository documentRepository ;
    private final SupervisionFileService fileService ;

    public UserService(UserRepository userRepository ,
    FormateurRepository formateurRepository ,
    DocumentRepository documentRepository ,
    SupervisionFileService fileService) {
        this.userRepository = userRepository;
        this.formateurRepository = formateurRepository;
        this.documentRepository=documentRepository;
        this.fileService = fileService ;
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

    @Transactional
public Formateur registerFormateurWithDocuments(Formateur formateur, 
                                             List<MultipartFile> certificats, 
                                             List<MultipartFile> experiences) throws IOException {
    // Initialiser les valeurs par défaut
    formateur.setStatus(UserStatus.PENDING);
    formateur.setDateInscription(new Date());
    formateur.setCoursPublies(0L);
    formateur.setCoursEnAttente(0L);
    formateur.setEtudiantsAbonnes(0L);

    Formateur savedFormateur = formateurRepository.save(formateur);
    
    List<Document> documents = new ArrayList<>();
    
    if (certificats != null) {
        for (MultipartFile file : certificats) {
            documents.add(createDocument(file, "CERTIFICAT", savedFormateur));
        }
    }
    
    if (experiences != null) {
        for (MultipartFile file : experiences) {
            documents.add(createDocument(file, "EXPERIENCE", savedFormateur));
        }
    }
    
    documentRepository.saveAll(documents);
    savedFormateur.setDocuments(documents);
    
    return savedFormateur;
}
    
    public Formateur addDocumentsToFormateur(Long formateurId, 
    List<MultipartFile> certificats, 
    List<MultipartFile> experiences) throws IOException {
Formateur formateur = formateurRepository.findById(formateurId)
.orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

// Solution 1: Spécifier explicitement le type générique
List<Document> documents = new ArrayList<Document>();

// Solution alternative (plus moderne, Java 7+):
// List<Document> documents = new ArrayList<>(formateur.getDocuments());

// Ajout des nouveaux certificats
if (certificats != null) {
for (MultipartFile file : certificats) {
documents.add(createDocument(file, "CERTIFICAT", formateur));
}
}

// Ajout des nouvelles expériences
if (experiences != null) {
for (MultipartFile file : experiences) {
documents.add(createDocument(file, "EXPERIENCE", formateur));
}
}

documentRepository.saveAll(documents);
formateur.setDocuments(documents);

return formateurRepository.save(formateur);
}
    
    public List<Document> getFormateurDocuments(Long formateurId) {
        return documentRepository.findByFormateurId(formateurId);
    }
    
    public List<Document> getFormateurDocumentsByType(Long formateurId, String documentType) {
        return documentRepository.findByFormateurIdAndDocumentType(formateurId, documentType);
    }
    
    /**
     * Méthode utilitaire pour créer un document à partir d'un fichier
     */
    private Document createDocument(MultipartFile file, String documentType, Formateur formateur) throws IOException {
        validateFileType(file);
        String fileUrl = fileService.storeFile(file);
        
        Document document = new Document();
        document.setTitle(extractFileNameWithoutExtension(file.getOriginalFilename()));
        document.setFileUrl(fileUrl);
        document.setDocumentType(documentType);
        document.setFormateur(formateur);
        
        return document;
    }
    
    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (!isSupportedFileType(contentType)) {
            throw new IllegalArgumentException("Format de fichier non supporté: " + contentType);
        }
    }
    
    private boolean isSupportedFileType(String contentType) {
        return contentType != null && (
            contentType.equals("application/pdf") || 
            contentType.startsWith("image/") ||
            contentType.equals("application/msword") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
        );
    }
    
    private String extractFileNameWithoutExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "Sans titre";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
    }
}
