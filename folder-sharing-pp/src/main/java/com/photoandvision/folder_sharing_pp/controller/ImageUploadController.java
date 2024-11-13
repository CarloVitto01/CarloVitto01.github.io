package com.photoandvision.folder_sharing_pp.controller;

import com.photoandvision.folder_sharing_pp.entity.Image;
import com.photoandvision.folder_sharing_pp.repo.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
@CrossOrigin("*")
@RequestMapping("/api/images")
public class ImageUploadController {
    @Autowired
    private ImageRepository imageRepository;

    // Imposta un percorso assoluto per la directory di upload
    @Value("${upload.dir}")
    private String uploadDir;

    // Endpoint per ottenere l'elenco di tutte le immagini
    @GetMapping
    public ResponseEntity<List<Image>> listImages() {
        List<Image> images = imageRepository.findAll();
        return ResponseEntity.ok(images); // Restituisce un JSON con l'elenco delle immagini
    }

    // Endpoint per caricare un'immagine in una cartella specificata
    @PostMapping("/upload/{folderName}")
    public ResponseEntity<String> uploadImage(@PathVariable String folderName, @RequestParam("file") MultipartFile file) {
        // Controlla se il file è vuoto
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty. Please select a file to upload.");
        }

        try {
            // Crea la directory se non esiste
            Path folderPath = Paths.get(uploadDir, folderName); // Crea il percorso della cartella
            if (!Files.exists(folderPath)) {
                // Se la cartella non esiste, creala
                Files.createDirectories(folderPath);
            }

            // Estrai il nome del file dal MultipartFile
            String fileName = file.getOriginalFilename(); // Ottiene il nome originale del file
            if (fileName == null) {
                return ResponseEntity.badRequest().body("File name is not valid."); // Controlla se il nome del file è valido
            }

            // Salva il file nella cartella specificata
            Path filePath = folderPath.resolve(fileName); // Crea il percorso completo del file
            file.transferTo(filePath); // Trasferisce il file nella directory

            // Salva i dettagli dell'immagine nel database
            Image image = new Image(); // Crea un nuovo oggetto immagine
            image.setName(fileName); // Usa il nome del file come nome dell'immagine
            image.setUrl(filePath.toString()); // Imposta l'URL del file (potresti voler usare solo il nome del file qui)
            imageRepository.save(image); // Salva l'immagine nel database

            return ResponseEntity.ok("File uploaded successfully."); // Restituisce una risposta di successo
        } catch (IOException e) {
            e.printStackTrace(); // Stampa lo stack trace in caso di errore
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage()); // Restituisce un errore interno
        }
    }

    // Endpoint per ottenere le immagini in una cartella specificata
    @GetMapping("/folder/{folderName}")
    public ResponseEntity<List<Image>> getImagesByFolder(@PathVariable String folderName) {
        try {
            // Crea il percorso della cartella
            Path folderPath = Paths.get(uploadDir, folderName);

            // Controlla se la cartella esiste
            if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
                return ResponseEntity.badRequest().body(null); // Restituisce un errore se la cartella non esiste
            }

            // Ottieni tutti i file nella cartella
            List<Image> images = imageRepository.findAll(); // Ottieni tutte le immagini dal database
            List<Image> imagesInFolder = new ArrayList<>();

            for (Image image : images) {
                // Controlla se l'immagine è nella cartella specificata
                if (image.getUrl().startsWith(folderPath.toString())) {
                    imagesInFolder.add(image);
                }
            }

            return ResponseEntity.ok(imagesInFolder); // Restituisce le immagini trovate nella cartella
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null); // Restituisce un errore interno del server
        }
    }
}