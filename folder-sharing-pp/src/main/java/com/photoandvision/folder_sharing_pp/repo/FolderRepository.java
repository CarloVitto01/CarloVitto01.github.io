package com.photoandvision.folder_sharing_pp.repo;


import com.photoandvision.folder_sharing_pp.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
}
