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
import com.example.supervision.repositories.ApprenantRepository;
import com.example.supervision.repositories.ChapterRepository;
import com.example.supervision.repositories.CourseRepository;


@Service

public class CourseService {

    private final CourseRepository repository;
    private final ChapterRepository chapterRepository;
    private final SupervisionFileService fileService;
   private final ApprenantRepository apprenantRepository;


    public CourseService(CourseRepository repository,
                   ChapterRepository chapterRepository,
                   SupervisionFileService fileService,
                   ApprenantRepository apprenantRepository) {
    this.repository = repository;
    this.chapterRepository = chapterRepository;
    this.fileService = fileService;
    this.apprenantRepository = apprenantRepository;
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
    // Récupère d'abord les vrais données
    List<Object[]> rawData = repository.findCoursesWithStudentCount();
    
    // Si vide, retourne des données de test
    if(rawData.isEmpty()) {
        List<Map<String, Object>> testData = new ArrayList<>();
        
        Map<String, Object> course1 = new HashMap<>();
        course1.put("courseId", "550e8400-e29b-41d4-a716-446655440000");
        course1.put("titre", "Introduction au Fiqh");
        course1.put("domaine", "Fiqh");
        course1.put("studentCount", 15);
        course1.put("chapterCount", 5);
        testData.add(course1);

        Map<String, Object> course2 = new HashMap<>();
        course2.put("courseId", "550e8400-e29b-41d4-a716-446655440001");
        course2.put("titre", "Histoire Islamique");
        course2.put("domaine", "History");
        course2.put("studentCount", 8);
        course2.put("chapterCount", 3);
        testData.add(course2);

        return testData;
    }
    
    // Sinon, traite les vraies données
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
    Cours cours = repository.findById(coursId)
        .orElseThrow(() -> new RuntimeException("Cours non trouvé"));
    
    Apprenant apprenant = apprenantRepository.findById(apprenantId)
        .orElseThrow(() -> new RuntimeException("Apprenant non trouvé"));
    
    // Vérification optimisée
    if (cours.getApprenants().contains(apprenant)) {
        throw new RuntimeException("L'apprenant est déjà inscrit à ce cours");
    }
    
    // Ajout unidirectionnel (seulement du côté Cours)
    cours.getApprenants().add(apprenant);
    repository.save(cours);
}
}