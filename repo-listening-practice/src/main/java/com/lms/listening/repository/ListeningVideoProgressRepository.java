package com.lms.listening.repository;

import com.lms.listening.entity.ListeningVideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ListeningVideoProgressRepository extends JpaRepository<ListeningVideoProgress, String> {
    Optional<ListeningVideoProgress> findByUserIdAndStudySetIdAndVideoCode(String userId, String studySetId, String videoCode);
    List<ListeningVideoProgress> findByUserIdAndStudySetId(String userId, String studySetId);
}
