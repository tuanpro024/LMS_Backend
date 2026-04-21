package com.lms.kanjiorigin.repository;

import com.lms.kanjiorigin.entity.KanjiStudySetProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KanjiStudySetProgressRepository extends JpaRepository<KanjiStudySetProgress, String> {
    Optional<KanjiStudySetProgress> findByUserIdAndStudySetId(String userId, String studySetId);

    void deleteByStudySetId(String studySetId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM KanjiStudySetProgress p WHERE p.deleted = true")
    void purgeSoftDeleted();
}
