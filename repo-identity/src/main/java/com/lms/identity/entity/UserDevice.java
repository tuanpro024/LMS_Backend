package com.lms.identity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_devices")
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String deviceId;

    private String deviceName;
    
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;
    
    private String os;
    private String browser;
    private String location;
    private LocalDateTime lastLogin;
}