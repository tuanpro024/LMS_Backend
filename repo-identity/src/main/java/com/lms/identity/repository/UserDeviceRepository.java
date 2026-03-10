package com.lms.identity.repository;

import com.lms.identity.entity.DeviceType;
import com.lms.identity.entity.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeviceRepository extends JpaRepository<UserDevice, String> {
    Optional<UserDevice> findByDeviceId(String deviceId);

    Optional<UserDevice> findByUser_IdAndDeviceId(String userId, String deviceId);

    long countByUser_IdAndDeviceType(String userId, DeviceType deviceType);

    UserDevice findFirstByUser_IdAndDeviceTypeOrderByLastLoginAsc(String userId, DeviceType deviceType);

    List<UserDevice> findAllByUser_Id(String userId);

    Optional<UserDevice> findByIdAndUser_Id(String id, String userId);
}