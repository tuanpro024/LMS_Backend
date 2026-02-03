package com.lms.identity.repository;

import com.lms.identity.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, String> {
    List<LoginHistory> findByUser_IdOrderByLoginTimeDesc(String userId);
}