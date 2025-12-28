package com.lms.identity.entity;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.lms.common.jpa.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "users")
public class User extends BaseEntity {

    @Indexed(unique = true)
    private String email;

    private String password;

    private String fullName;

    private String phoneNumber;

    private String avatarUrl;

    private String address;

    private UserStatus status;

    private boolean emailVerified;

    @DBRef
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
