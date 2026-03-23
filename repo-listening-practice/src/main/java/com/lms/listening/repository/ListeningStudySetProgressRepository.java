package com.lms.listening.repository;

import com.lms.listening.entity.ListeningStudySetProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ListeningStudySetProgressRepository extends JpaRepository<ListeningStudySetProgress, String> {
    Optional<ListeningStudySetProgress> findByUserIdAndStudySetId(String userId, String studySetId);
}
