package com.marketingagencybackend.repository;

import com.marketingagencybackend.entity.SubscriptionPurchase;
import com.marketingagencybackend.enums.SubscriptionApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionPurchaseRepository extends JpaRepository<SubscriptionPurchase, Long> {
    List<SubscriptionPurchase> findByClientId(Long clientId);
    List<SubscriptionPurchase> findByStatus(SubscriptionApprovalStatus status);
}
