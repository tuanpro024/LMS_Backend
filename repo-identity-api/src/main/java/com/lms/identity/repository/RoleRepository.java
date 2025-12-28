package com.lms.identity.repository;

import com.lms.identity.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.lms.identity.entity.RoleName;

import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByName(RoleName name);
}
