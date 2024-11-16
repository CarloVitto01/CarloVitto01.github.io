package com.photoandvision.folder_sharing_pp.repo;

import com.photoandvision.folder_sharing_pp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}

