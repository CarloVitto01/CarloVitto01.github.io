package com.photoandvision.folder_sharing_pp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@RestController // Indica che questa classe è un controller REST
@CrossOrigin("*") // Permette le richieste CORS da qualsiasi origine
@RequestMapping("/api/folders") // Mappa le richieste a questo controller all'URL /api/folders
public class FolderController {

    private static final Logger logger = LoggerFactory.getLogger(FolderController.class); // Logger per registrare eventi
    private final String baseDirectory = "E:\\PhotoAndVision\\"; // Modifica il percorso base per le cartelle

    // Endpoint per creare una nuova cartella
    @PostMapping
    public ResponseEntity<String> createFolder(@RequestBody FolderRequest folderRequest) {
        String folderName = folderRequest.getName(); // Ottiene il nome della cartella dalla richiesta

        // Validazione del nome della cartella
        if (folderName == null || folderName.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Il nome della cartella non può essere vuoto."); // Restituisce un errore se il nome è vuoto
        }

        File folder = new File(baseDirectory + folderName); // Crea un oggetto File per la nuova cartella

        // Controlla se la cartella esiste già
        if (folder.exists()) {
            logger.warn("Tentativo di creare una cartella esistente: {}", folderName); // Registra un avviso
            return ResponseEntity.status(HttpStatus.CONFLICT).body("La cartella esiste già."); // Restituisce un errore di conflitto
        }

        // Crea la cartella
        if (folder.mkdirs()) {
            logger.info("Cartella creata con successo: {}", folderName); // Registra un'informazione
            return ResponseEntity.status(HttpStatus.CREATED).body("Cartella creata con successo: " + folderName); // Restituisce una risposta di successo
        } else {
            logger.error("Errore nella creazione della cartella: {}", folderName); // Registra un errore
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore nella creazione della cartella."); // Restituisce un errore interno
        }
    }

    // Endpoint per ottenere tutte le cartelle
    @GetMapping
    public ResponseEntity<List<String>> getAllFolders() {
        File baseDir = new File(baseDirectory); // Crea un oggetto File per la directory base
        File[] directories = baseDir.listFiles(File::isDirectory); // Ottiene tutte le directory nella base

        List<String> folderNames = new ArrayList<>(); // Lista per memorizzare i nomi delle cartelle
        if (directories != null) {
            for (File dir : directories) {
                folderNames.add(dir.getName()); // Aggiunge il nome della cartella alla lista
            }
        }

        logger.info("Elenco delle cartelle recuperato con successo."); // Registra un'informazione
        return ResponseEntity.ok(folderNames); // Restituisce l'elenco delle cartelle
    }

    // Nuovo endpoint per ottenere le immagini in una cartella specifica
    @GetMapping("/{folderName}/images")
    public ResponseEntity<List<String>> getImagesByFolder(@PathVariable String folderName) {
        File folder = new File(baseDirectory + folderName); // Crea un oggetto File per la cartella specificata
        if (!folder.exists() || !folder.isDirectory()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Restituisce un errore se la cartella non esiste
        }

        List<String> imageNames = new ArrayList<>(); // Lista per memorizzare i nomi delle immagini
        File[] files = folder.listFiles(); // Ottiene tutti i file nella cartella
        if (files != null) {
            for (File file : files) {
                if (isImageFile(file)) { // Controlla se il file è un'immagine
                    imageNames.add(file.getName()); // Aggiunge il nome dell'immagine alla lista
                }
            }
        }

        logger.info("Immagini recuperate per la cartella: {}", folderName); // Registra un'informazione
        return ResponseEntity.ok(imageNames); // Restituisce l'elenco delle immagini trovate nella cartella
    }

    // Nuovo endpoint per servire le immagini
    @GetMapping("/{folderName}/images/{imageName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String folderName, @PathVariable String imageName) {
        File imageFile = new File(baseDirectory + folderName + File.separator + imageName); // Crea un oggetto File per l'immagine specificata
        if (!imageFile.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Restituisce un errore se l'immagine non esiste
        }

        try {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath()); // Legge i byte dell'immagine
            String mimeType = URLConnection.guessContentTypeFromName(imageFile.getName()); // Ottiene il tipo MIME dell'immagine
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType != null ? mimeType : "application/octet-stream")) // Imposta il tipo di contenuto
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageFile.getName() + "\"") // Aggiungi l'intestazione per il download
                    .body(imageBytes); // Restituisce i byte dell'immagine
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Restituisce un errore interno in caso di eccezione
        }
    }

    // Metodo privato per verificare se un file è un'immagine
    private boolean isImageFile(File file) {
        String[] imageExtensions = { "jpg", "jpeg", "png", "gif", "bmp", "tiff" }; // Estensioni valide per le immagini
        String fileName = file.getName().toLowerCase(); // Ottiene il nome del file in minuscolo
        for (String extension : imageExtensions) {
            if (fileName.endsWith(extension)) {
                return true; // Restituisce true se il file ha un'estensione valida
            }
        }
        return false; // Restituisce false se il file non è un'immagine
    }

    // Classe interna per la richiesta di creazione della cartella
    public static class FolderRequest {
        private String name; // Nome della cartella

        public String getName() {
            return name; // Restituisce il nome della cartella
        }

        public void setName(String name) {
            this.name = name; // Imposta il nome della cartella
        }
    }

    // Nuovo endpoint per cancellare una o più cartelle
    @DeleteMapping
    public ResponseEntity<String> deleteFolders(@RequestBody List<String> folderNames) {
        if (folderNames == null || folderNames.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Nessun nome di cartella fornito."); // Restituisce un errore se non ci sono nomi
        }

        StringBuilder responseMessage = new StringBuilder();
        for (String folderName : folderNames) {
            File folder = new File(baseDirectory + folderName); // Crea un oggetto File per la cartella

            // Controlla se la cartella esiste
            if (!folder.exists() || !folder.isDirectory()) {
                responseMessage.append("La cartella ").append(folderName).append(" non esiste.\n");
                continue; // Passa alla prossima cartella
            }

            // Cancella la cartella
            if (deleteDirectory(folder)) {
                responseMessage.append("Cartella ").append(folderName).append(" eliminata con successo.\n");
            } else {
                responseMessage.append("Errore nell'eliminazione della cartella ").append(folderName).append(".\n");
            }
        }

        return ResponseEntity.ok(responseMessage.toString()); // Restituisce un messaggio di successo o errore
    }

    // Metodo privato per eliminare una directory e il suo contenuto
    private boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles(); // Ottiene tutti i file nella directory
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file); // Ricorsivamente elimina le sottodirectory
                } else {
                    file.delete(); // Elimina il file
                }
            }
        }
        return directory.delete(); // Elimina la directory stessa
    }
}