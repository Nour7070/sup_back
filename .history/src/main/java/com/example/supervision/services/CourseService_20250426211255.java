package com.example.supervision.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.supervision.classes.Apprenant;
import com.example.supervision.classes.Chapter;
import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.repositories.ChapterRepository;
import com.example.supervision.repositories.CourseRepository;


@Service

public class CourseService {

    private final CourseRepository repository;
    private final ChapterRepository chapterRepository;
    private final SupervisionFileService fileService;

    public CourseService (CourseRepository repository ,
    ChapterRepository chapterRepository ,
    SupervisionFileService fileService ){
        this.repository=repository;
        this.chapterRepository= chapterRepository;
        this.fileService= fileService ;
    }
    public boolean validateCourse(UUID id, boolean approve) {
        Optional<Cours> optionalCours = repository.findById(id);
        if (optionalCours.isEmpty()) {
            return false;
        }

        Cours cours = optionalCours.get();
        cours.setStatus(approve ? CourseStatus.APPROVED : CourseStatus.REJECTED);
        repository.save(cours);
        return true;
    }

     @Transactional
    public Cours createCourseWithChapters(Cours courseRequest, List<MultipartFile> files) throws IOException {
        List<Chapter> chapters = processUploadedFiles(files);
        
        courseRequest.setStatus(CourseStatus.PENDING);
        courseRequest.setChapters(chapters);
        Cours savedCourse = repository.save(courseRequest);
        //lier les chapitres au cours puis les sauvegarder
        saveChaptersWithCourseReference(chapters, savedCourse);
        
        return savedCourse;
    }

    private List<Chapter> processUploadedFiles(List<MultipartFile> files) throws IOException {
        List<Chapter> chapters = new ArrayList<>();
        for (MultipartFile file : files) {
            //Nvalidiw le type de chaque fichier
            validateFileType(file);
            //Ntel3o le fichier et nrecuperiw son URL 
            String fileUrl = storeFileAndGetUrl(file);
            //Créer un chapitre par fichier
            chapters.add(createChapterFromFile(file, fileUrl));
        }
        
        return chapters;
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        //PDF ou PPTX sinon exception
        if (!isSupportedFileType(contentType)) {
            throw new IllegalArgumentException("Format de fichier non supporté: " + contentType);
        }
    }

    private boolean isSupportedFileType(String contentType) {
        return contentType != null && (
            contentType.equals("application/pdf") || 
            contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")||
            contentType.equals("application/octet-stream")
        );
    }

    // Retourner l'url du fichier mour ma SuperviseurFileService  uploadah
    private String storeFileAndGetUrl(MultipartFile file) throws IOException {
        return fileService.storeFile(file);
    }

    //Créer un chapitre par fichier
    private Chapter createChapterFromFile(MultipartFile file, String fileUrl) {
        Chapter chapter = new Chapter();
        chapter.setTitle(extractFileNameWithoutExtension(file.getOriginalFilename()));
        chapter.setFileUrl(fileUrl);
        return chapter;
    }

    // Nom du fichier sans extension
    private String extractFileNameWithoutExtension(String filename) {
        return filename != null ? filename.replaceFirst("[.][^.]+$", "") : "Sans titre";
    }

    //Sauvegarde chaque chapitre en base avec une référence au cours
    private void saveChaptersWithCourseReference(List<Chapter> chapters, Cours cours) {
        chapters.forEach(chapter -> {
            chapter.setCours(cours);
            chapterRepository.save(chapter);
        });
    }

    //Avoir les cours par formateurs ala hssab le statut 
    @Transactional(readOnly = true)
    public List<Cours> getCoursesByFormateur(Long formateurId, CourseStatus status) {
      return repository.findByFormateurWithChapters(formateurId, status);
    }

    //Yehsseb les cours ala hssab leurs statuts
    public long countCoursesByFormateurAndStatus(Long formateurId, CourseStatus status) {
       return repository.countByFormateurIdAndStatus(formateurId, status);
    }

    public List<Object[]> getCoursesCreatedPerDomainByMonth(int year) {
      return repository.findCoursesCreatedPerDomainByMonth(year);
    }

    public List<Map<String, Object>> getCoursesStatsByDomain() {
    List<Object[]> rawData = repository.findCoursesCountByDomain();
    List<Map<String, Object>> result = new ArrayList<>();
    
    for (Object[] row : rawData) {
        Map<String, Object> map = new HashMap<>();
        map.put("domaine", row[0]);
        map.put("count", row[1]);
        map.put("approvedCount", row[2]); // Pour afficher que les cours approuvés
        result.add(map);
    }
    
    return result;
}

public List<Map<String, Object>> getCoursesPopularity() {
    // Vous aurez besoin d'ajouter une relation entre Cours et Étudiants
    // Ou utiliser une table d'inscriptions si elle existe
    List<Object[]> rawData = repository.findCoursesWithStudentCount();
    List<Map<String, Object>> result = new ArrayList<>();
    
    for (Object[] row : rawData) {
        Map<String, Object> map = new HashMap<>();
        map.put("courseId", row[0]);
        map.put("titre", row[1]);
        map.put("domaine", row[2]);
        map.put("studentCount", row[3]);
        map.put("chapterCount", row[4]);
        result.add(map);
    }
    
    return result;
}

   @Transactional
public void inscrireApprenantAUcours(UUID coursId, Long apprenantId) {
    // Dans une vraie implémentation, vous devriez vérifier que le cours et l'apprenant existent
    // Ici on fait une version simplifiée pour le test
    
    Cours cours = repository.findById(coursId)
        .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
    
    // Normalement vous devriez avoir un ApprenantRepository pour charger l'apprenant
    // Pour le test, on crée un apprenant minimal
    Apprenant apprenant = new Apprenant();
    apprenant.setId(apprenantId); // Suppose que votre User/Apprenant a un ID Long
    
    cours.addApprenant(apprenant);
    repository.save(cours);
}
}