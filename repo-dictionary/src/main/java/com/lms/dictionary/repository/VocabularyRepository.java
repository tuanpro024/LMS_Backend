package com.lms.dictionary.repository;

import com.lms.dictionary.entity.Vocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

        @Query("SELECT DISTINCT v FROM Vocabulary v " +
                        "LEFT JOIN v.meanings m " +
                        "WHERE ((:keySearch IS NULL OR :keySearch = '') " +
                        "OR (v.hanzi LIKE %:keySearch% " +
                        "OR v.pinyin LIKE %:keySearch% " +
                        "OR m.meaning LIKE %:keySearch%)) " +
                        "AND (:isSingleVocab IS NULL OR v.isSingleVocab = :isSingleVocab) " +
                        "AND v.deleted = false " +
                        "AND (:hskLevel IS NULL OR v.hskLevel = :hskLevel) " +
                        "AND (:wordType IS NULL OR :wordType = '' OR v.wordType = :wordType)")
        Page<Vocabulary> searchByQuery(@Param("keySearch") String keySearch,
                        @Param("isSingleVocab") Boolean isSingleVocab,
                        @Param("hskLevel") Integer hskLevel,
                        @Param("wordType") String wordType,
                        Pageable pageable);

        @Query("SELECT v FROM Vocabulary v WHERE v.deleted = false ORDER BY RAND()")
        Page<Vocabulary> findSuggestions(Pageable pageable);

        Optional<Vocabulary> findByHanziAndPinyinAndDeletedFalse(String hanzi, String pinyin);

        List<Vocabulary> findByHanziAndDeletedFalse(String hanzi);

        boolean existsByHanziAndDeletedFalse(String hanzi);
}
