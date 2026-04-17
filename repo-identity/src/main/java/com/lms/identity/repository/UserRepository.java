package com.lms.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.lms.identity.entity.User;
import com.lms.identity.entity.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByGoogleId(String googleId);

    boolean existsByGoogleId(String googleId);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :emailContains, '%')) AND u.status = :status AND u.deleted = false")
    List<User> findByEmailContainingIgnoreCaseAndStatus(@Param("emailContains") String emailContains,
            @Param("status") UserStatus status);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :emailContains, '%')) AND u.deleted = false")
    List<User> findByEmailContainingIgnoreCase(@Param("emailContains") String emailContains);

    @Query("SELECT u FROM User u WHERE u.status = :status AND u.deleted = false")
    List<User> findByStatus(@Param("status") UserStatus status);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.deleted = false")
    List<User> findByRolesName(@Param("roleName") com.lms.identity.entity.RoleName roleName);

    @Query("SELECT u.id FROM User u WHERE u.deleted = false AND u.status = :status")
    Page<String> findUserIdsByStatus(@Param("status") UserStatus status, Pageable pageable);

    List<User> findAllByPremiumTrueAndPremiumExpiryDateBetween(java.time.Instant start, java.time.Instant end);

    List<User> findAllByPremiumTrueAndPremiumExpiryDateBefore(java.time.Instant now);
}
