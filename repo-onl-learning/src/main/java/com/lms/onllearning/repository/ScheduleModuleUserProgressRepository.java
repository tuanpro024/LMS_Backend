package com.lms.onllearning.repository;

import com.lms.onllearning.entity.ScheduleModuleUserProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ScheduleModuleUserProgressRepository extends JpaRepository<ScheduleModuleUserProgress, String> {

    Optional<ScheduleModuleUserProgress> findByModuleIdAndUserIdAndDeletedFalse(String moduleId, String userId);

    List<ScheduleModuleUserProgress> findByModuleIdInAndUserIdAndDeletedFalse(Collection<String> moduleIds, String userId);

    List<ScheduleModuleUserProgress> findByModuleIdInAndDeletedFalse(Collection<String> moduleIds);

    List<ScheduleModuleUserProgress> findByDeletedFalse();
}
