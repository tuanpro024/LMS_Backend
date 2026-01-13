package com.lms.identity.repository;

import com.lms.identity.dto.request.AdminUserFilterRequest;
import com.lms.identity.entity.Role;
import com.lms.identity.entity.RoleName;
import com.lms.identity.entity.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> buildSpecification(AdminUserFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            // Always exclude deleted users
            predicate = criteriaBuilder.and(predicate,
                    criteriaBuilder.equal(root.get("deleted"), false));

            // Filter by email (case-insensitive contains)
            if (filter.getEmailContains() != null && !filter.getEmailContains().isBlank()) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("email")),
                                "%" + filter.getEmailContains().toLowerCase() + "%"));
            }

            // Filter by status
            if (filter.getStatus() != null) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }

            // Filter by role
            if (filter.getRole() != null && !filter.getRole().isBlank()) {
                Join<User, Role> rolesJoin = root.join("roles", JoinType.INNER);
                try {
                    RoleName roleName = RoleName.valueOf(filter.getRole());
                    predicate = criteriaBuilder.and(predicate,
                            criteriaBuilder.equal(rolesJoin.get("name"), roleName));
                } catch (IllegalArgumentException e) {
                    // Invalid role name, will return no results
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.disjunction());
                }
            }

            return predicate;
        };
    }
}
