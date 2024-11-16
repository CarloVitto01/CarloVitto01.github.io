package com.photoandvision.folder_sharing_pp.repo;

import com.photoandvision.folder_sharing_pp.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Image findByName(String name); // Metodo per trovare un'immagine per nome
}