package com.lms.videocourse.repository;

import com.lms.videocourse.entity.SyllabusSyncRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SyllabusSyncRunRepository extends JpaRepository<SyllabusSyncRun, String> {
    Page<SyllabusSyncRun> findAllByOrderByStartedAtDesc(Pageable pageable);
    Optional<SyllabusSyncRun> findFirstByOrderByStartedAtDesc();
}
