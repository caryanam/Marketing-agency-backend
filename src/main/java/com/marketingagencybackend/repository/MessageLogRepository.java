package com.marketingagencybackend.repository;

import com.marketingagencybackend.entity.MessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageLogRepository extends JpaRepository<MessageLog, Long> {
    List<MessageLog> findByClientId(Long clientId);
    List<MessageLog> findByCampaignId(Long campaignId);
}
