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
    public ResponseEntity<String> uploadImage(@PathVariable String folderName, @RequestParam("file") MultipartFile[] files) {
        // Controlla se i file sono vuoti
        if (files.length == 0) {
            return ResponseEntity.badRequest().body("No files selected. Please select at least one file to upload.");
        }

        StringBuilder responseMessage = new StringBuilder();

        try {
            // Crea la directory se non esiste
            Path folderPath = Paths.get(uploadDir, folderName); // Crea il percorso della cartella
            if (!Files.exists(folderPath)) {
                // Se la cartella non esiste, creala
                Files.createDirectories(folderPath);
            }

            for (MultipartFile file : files) {
                // Controlla se il file è vuoto
                if (file.isEmpty()) {
                    continue; // Salta i file vuoti
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

                responseMessage.append(fileName).append(" uploaded successfully.\n"); // Aggiungi il messaggio di successo
            }

            return ResponseEntity.ok(responseMessage.toString()); // Restituisce una risposta di successo
        } catch (IOException e) {
            e.printStackTrace(); // Stampa lo stack trace in caso di errore
            return ResponseEntity.status(500).body("Failed to upload files: " + e.getMessage()); // Restituisce un errore interno
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
    // Endpoint per eliminare un'immagine
    @DeleteMapping("/delete/{folderName}/{fileName}")
    public ResponseEntity<String> deleteImage(@PathVariable String folderName, @PathVariable String fileName) {
        try {
            // Crea il percorso del file da eliminare
            Path filePath = Paths.get(uploadDir, folderName, fileName);

            // Controlla se il file esiste
            if (!Files.exists(filePath)) {
                return ResponseEntity.badRequest().body("File not found."); // Restituisce un errore se il file non esiste
            }

            // Elimina il file dal filesystem
            Files.delete(filePath);

            // Rimuovi l'immagine dal database
            Image image = imageRepository.findByName(fileName); // Assicurati di avere un metodo per trovare l'immagine per nome
            if (image != null) {
                imageRepository.delete(image); // Elimina l'immagine dal database
            }

            return ResponseEntity.ok(fileName + " deleted successfully."); // Restituisce una risposta di successo
        } catch (IOException e) {
            e.printStackTrace(); // Stampa lo stack trace in caso di errore
            return ResponseEntity.status(500).body("Failed to delete file: " + e.getMessage()); // Restituisce un errore interno
        }
    }
}