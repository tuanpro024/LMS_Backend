package com.lms.payment.repository;

import com.lms.payment.entity.UserPackageAccess;
import com.lms.payment.entity.enums.AccessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPackageAccessRepository extends JpaRepository<UserPackageAccess, String> {
    Optional<UserPackageAccess> findByUserIdAndPackageIdAndStatus(String userId, String packageId, AccessStatus status);
    List<UserPackageAccess> findByUserIdAndStatus(String userId, AccessStatus status);
}
