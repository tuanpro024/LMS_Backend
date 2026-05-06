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
    @Mapping(target = "isPremium", expression = "java(user.isPremium())")
    @Mapping(target = "shortDescription", source = "shortDescription")
    @Mapping(target = "fullDescription", source = "fullDescription")
    @Mapping(target = "teachingStyle", source = "teachingStyle")
    @Mapping(target = "qualification", source = "qualification")
    @Mapping(target = "videoIntroLink", source = "videoIntroLink")
    ProfileResponse toProfile(User user);

    @Mapping(target = "roles", expression = "java(toRoleNames(user.getRoles()))")
    @Mapping(target = "shortDescription", source = "shortDescription")
    @Mapping(target = "fullDescription", source = "fullDescription")
    @Mapping(target = "teachingStyle", source = "teachingStyle")
    @Mapping(target = "qualification", source = "qualification")
    @Mapping(target = "videoIntroLink", source = "videoIntroLink")
    AdminUserResponse toAdmin(User user);

    default Set<String> toRoleNames(Set<Role> roles) {
        return roles == null
                ? Set.of()
                : roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }

}
