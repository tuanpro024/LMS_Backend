package com.lms.identity.entity;

import jakarta.persistence.*;
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
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_google_id", columnList = "googleId")
})
public class User extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = true)
    private String googleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    private String password;

    private String fullName;

    private String phoneNumber;

    @Column(name = "avatar_url")
    private String avatarUrl;

    private String address;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private boolean emailVerified;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(name = "is_premium")
    @Builder.Default
    private Boolean premium = false;

    public boolean isPremium() {
        return premium != null && premium && 
               (premiumExpiryDate == null || premiumExpiryDate.isAfter(java.time.Instant.now()));
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    @Column(name = "premium_expiry_date")
    private java.time.Instant premiumExpiryDate;
}
