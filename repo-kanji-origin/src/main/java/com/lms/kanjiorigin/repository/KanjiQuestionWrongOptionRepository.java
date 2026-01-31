package com.lms.kanjiorigin.repository;

import com.lms.kanjiorigin.entity.KanjiQuestionWrongOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KanjiQuestionWrongOptionRepository extends JpaRepository<KanjiQuestionWrongOption, String> {
    void deleteByKanjiQuestionId(String questionId);
}
