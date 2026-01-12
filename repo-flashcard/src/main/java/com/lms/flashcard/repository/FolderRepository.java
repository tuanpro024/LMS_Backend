package com.lms.flashcard.repository;

import com.lms.flashcard.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends JpaRepository<Folder, String> {

    List<Folder> findByUserId(String userId);

    List<Folder> findByIsPrivateFalse();
}
