package com.lms.notification.repository;

import com.lms.notification.entity.ManualNotificationCampaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManualNotificationCampaignRepository extends JpaRepository<ManualNotificationCampaign, String> {
}
