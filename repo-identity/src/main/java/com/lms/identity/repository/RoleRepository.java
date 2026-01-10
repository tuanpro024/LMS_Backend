package com.lms.identity.repository;

import com.lms.identity.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import com.lms.identity.entity.RoleName;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(RoleName name);
}
