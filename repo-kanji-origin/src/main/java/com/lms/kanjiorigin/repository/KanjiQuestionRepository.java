package com.lms.kanjiorigin.repository;

import com.lms.kanjiorigin.entity.KanjiQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KanjiQuestionRepository extends JpaRepository<KanjiQuestion, String> {
    Optional<KanjiQuestion> findByIdAndDeletedFalse(String id);
    List<KanjiQuestion> findAllByDeletedFalse();
    boolean existsByContentAndDeletedFalse(String content);
    boolean existsByContentAndIdNotAndDeletedFalse(String content, String id);
    
    Page<KanjiQuestion> findAllByDeletedFalse(Pageable pageable);
    
    @Query("SELECT q FROM KanjiQuestion q WHERE q.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<KanjiQuestion> search(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT q FROM KanjiQuestion q WHERE q.deleted = false " +
           "AND (:keyword IS NULL OR LOWER(q.content) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<KanjiQuestion> searchList(@Param("keyword") String keyword);
    
    // For import
    Optional<KanjiQuestion> findByContentAndCorrectAnswerAndDeletedFalse(String content, String correctAnswer);
}
