package com.lms.learningpath.repository;

import com.lms.learningpath.entity.SectionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SectionModule entity.
 * Handles queries for modules within learning sections.
 */
@Repository
public interface SectionModuleRepository extends JpaRepository<SectionModule, String> {

    /**
     * Find all modules in a folder (section), ordered by moduleOrder
     */
    List<SectionModule> findByFolderIdOrderByModuleOrderAsc(String folderId);

    /**
     * Find a specific module in a folder by study set ID
     */
    Optional<SectionModule> findByFolderIdAndStudySetId(String folderId, String studySetId);

    /**
     * Count modules in a folder
     */
    long countByFolderId(String folderId);

    /**
     * Count required modules in a folder
     */
    @Query("SELECT COUNT(sm) FROM SectionModule sm WHERE sm.folderId = :folderId AND sm.isRequired = true")
    long countRequiredModulesByFolderId(@Param("folderId") String folderId);

    /**
     * Delete all modules in a folder
     */
    void deleteByFolderId(String folderId);

    /**
     * Find modules by module type
     */
    List<SectionModule> findByModuleType(com.lms.learningpath.entity.enums.ModuleType moduleType);
}
