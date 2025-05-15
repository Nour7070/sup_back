package com.example.supervision.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class FileContentService {

    @Value("${file.upload-dir}")
    private String uploadDir;

     public String extractContentFromStoredFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        try (InputStream is = Files.newInputStream(filePath)) {
            if (filename.endsWith(".pdf")) {
                try (PDDocument document = PDDocument.load(is)) {
                    return new PDFTextStripper().getText(document);
                }
            } else if (filename.endsWith(".pptx")) {
                return extractTextFromPptx(is);
            }
            throw new IllegalArgumentException("Type de fichier non supporté");
        }
    }
    private String extractTextFromPptx(InputStream is) throws IOException {
        StringBuilder content = new StringBuilder();
        
        try (XMLSlideShow ppt = new XMLSlideShow(is)) {
        
            content.append("Présentation: ").append(ppt.getSlides().size()).append(" slides\n\n");
            
            int slideIndex = 1;
            for (XSLFSlide slide : ppt.getSlides()) {
                content.append("=== SLIDE ").append(slideIndex++).append(" ===\n");
                
                if (slide.getTitle() != null) {
                    content.append("Titre: ").append(slide.getTitle()).append("\n");
                }
                
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        String text = textShape.getText();
                        if (text != null && !text.trim().isEmpty()) {
                            content.append(text).append("\n");
                        }
                    }
                }
                
                content.append("\n");
            }
        }
        
        String result = content.toString();
        // Log pour débogage
        System.out.println("Contenu extrait du PPTX (" + result.length() + " caractères):\n" + 
                          (result.length() > 100 ? result.substring(0, 100) + "..." : result));
        
        return result;
    }
}