package com.example.supervision.controllers;

import com.example.supervision.classes.Cours;
import com.example.supervision.classes.CourseStatus;
import com.example.supervision.repositories.CourseRepository;
import com.example.supervision.services.ActivityLogService;
import com.example.supervision.services.CourseService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/courses")

public class CourseController {
    private final CourseService service;
    private final ObjectMapper objectMapper;
    private final ActivityLogService  activityLogService ;
    private final  CourseRepository repository ;
    public CourseController(CourseService service ,
    ObjectMapper objectMapper ,
    ActivityLogService  activityLogService ,
    CourseRepository repository)  {
        this.service = service;
        this.objectMapper = objectMapper;
        this.activityLogService = activityLogService ;
        this.repository = repository;
    }
    
// hedi trouh mor kafka 
    @PostMapping(value = "/create-with-chapters", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Cours> createCourseWithChapters(
      @RequestPart("course") String courseJson,
      @RequestPart("files") List<MultipartFile> files) throws IOException {
    
      Cours courseRequest = objectMapper.readValue(courseJson, Cours.class);
      Cours savedCourse = service.createCourseWithChapters(courseRequest, files);
      activityLogService.log(
        "New course pending", 
        "Course \"" + savedCourse.getTitre() + "\" created and pending approval.",
        savedCourse.getFormateurId()
       );    
      return ResponseEntity.ok(savedCourse);
    }

//recuperer les cours d'un formateur ala hssab le status du cours 
@GetMapping("/formateurs/{formateurId}/courses")
public ResponseEntity<List<Cours>> getCoursesByFormateur(
    @PathVariable Long formateurId,
    @RequestParam(required = false) CourseStatus status) {
    
    List<Cours> courses = service.getCoursesByFormateur(formateurId, status);
    return ResponseEntity.ok(courses);
  }

  // hedi te3 le graphe des créations des cours par domaine
  @GetMapping("/stats")
  public List<Map<String, Object>> getCoursesCreatedPerDomain(@RequestParam int year) {
      List<Object[]> rawData = service.getCoursesCreatedPerDomainByMonth(year);
      List<Map<String, Object>> result = new ArrayList<>();
      for (Object[] row : rawData) {
          Map<String, Object> map = new HashMap<>();
          map.put("domaine", row[0]);
          map.put("mois", row[1]);
          map.put("count", row[2]);
          result.add(map);
      }
      return result;
  }
}