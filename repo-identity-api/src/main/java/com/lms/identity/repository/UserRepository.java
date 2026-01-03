package com.lms.identity.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import com.lms.identity.entity.User;
import com.lms.identity.entity.UserStatus;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByGoogleId(String googleId);
    boolean existsByGoogleId(String googleId);

    @Query("{ 'email': { $regex: ?0, $options: 'i' }, 'status': ?1, 'deleted': false }")
    List<User> findByEmailContainingIgnoreCaseAndStatus(String emailContains, UserStatus status);
    
    @Query("{ 'email': { $regex: ?0, $options: 'i' }, 'deleted': false }")
    List<User> findByEmailContainingIgnoreCase(String emailContains);
    
    @Query("{ 'status': ?0, 'deleted': false }")
    List<User> findByStatus(UserStatus status);
    
    @Query("{ 'roles.name': ?0, 'deleted': false }")
    List<User> findByRolesName(String roleName);
}
