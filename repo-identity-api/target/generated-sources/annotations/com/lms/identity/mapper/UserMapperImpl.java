package com.lms.identity.mapper;

import com.lms.identity.dto.response.AdminUserResponse;
import com.lms.identity.dto.response.ProfileResponse;
import com.lms.identity.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-09T14:02:17+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260101-2150, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public ProfileResponse toProfile(User user) {
        if ( user == null ) {
            return null;
        }

        ProfileResponse.ProfileResponseBuilder profileResponse = ProfileResponse.builder();

        profileResponse.address( user.getAddress() );
        profileResponse.avatarUrl( user.getAvatarUrl() );
        profileResponse.email( user.getEmail() );
        profileResponse.fullName( user.getFullName() );
        profileResponse.id( user.getId() );
        profileResponse.phoneNumber( user.getPhoneNumber() );
        profileResponse.status( user.getStatus() );

        profileResponse.roles( toRoleNames(user.getRoles()) );

        return profileResponse.build();
    }

    @Override
    public AdminUserResponse toAdmin(User user) {
        if ( user == null ) {
            return null;
        }

        AdminUserResponse.AdminUserResponseBuilder adminUserResponse = AdminUserResponse.builder();

        adminUserResponse.address( user.getAddress() );
        adminUserResponse.avatarUrl( user.getAvatarUrl() );
        adminUserResponse.email( user.getEmail() );
        adminUserResponse.fullName( user.getFullName() );
        adminUserResponse.id( user.getId() );
        adminUserResponse.phoneNumber( user.getPhoneNumber() );
        adminUserResponse.status( user.getStatus() );

        adminUserResponse.roles( toRoleNames(user.getRoles()) );

        return adminUserResponse.build();
    }
}
