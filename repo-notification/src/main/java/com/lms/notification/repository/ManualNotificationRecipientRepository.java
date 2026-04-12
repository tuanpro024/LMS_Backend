package com.lms.notification.repository;

import com.lms.notification.entity.ManualNotificationRecipient;
import com.lms.notification.entity.enums.RecipientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManualNotificationRecipientRepository extends JpaRepository<ManualNotificationRecipient, String> {
    Page<ManualNotificationRecipient> findByCampaignIdAndStatus(String campaignId, RecipientStatus status, Pageable pageable);
    
    long countByCampaignIdAndStatus(String campaignId, RecipientStatus status);
}
