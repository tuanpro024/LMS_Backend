package com.lms.identity.mapper;

import com.lms.identity.dto.response.AdminUserResponse;
import com.lms.identity.dto.response.ProfileResponse;
import com.lms.identity.entity.Role;
import com.lms.identity.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", expression = "java(toRoleNames(user.getRoles()))")
    ProfileResponse toProfile(User user);

    @Mapping(target = "roles", expression = "java(toRoleNames(user.getRoles()))")
    AdminUserResponse toAdmin(User user);

    default Set<String> toRoleNames(Set<Role> roles) {
        return roles == null
                ? Set.of()
                : roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }

}
