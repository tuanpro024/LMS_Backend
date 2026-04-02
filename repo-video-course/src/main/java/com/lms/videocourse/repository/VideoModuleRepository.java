package com.lms.videocourse.repository;

import com.lms.videocourse.entity.VideoModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoModuleRepository extends JpaRepository<VideoModule, String> {

    List<VideoModule> findByStepIdAndIsActiveTrueOrderByModuleOrderAsc(String stepId);

    List<VideoModule> findByStepIdOrderByModuleOrderAsc(String stepId);

    Optional<VideoModule> findByVideoCode(String videoCode);

    long countByStepIdAndIsActiveTrue(String stepId);

    List<VideoModule> findByContentSetIdAndModuleTypeAndIsActiveTrue(String contentSetId, com.lms.videocourse.entity.enums.ModuleType moduleType);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query(value = "UPDATE video_modules SET content_set_id = ?2 WHERE content_set_id = ?1", nativeQuery = true)
    int updateContentSetId(String oldId, String newId);
}
