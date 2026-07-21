package com.marketingagencybackend.dto;

import com.marketingagencybackend.enums.BillingBasis;
import com.marketingagencybackend.enums.SubscriptionApprovalStatus;
import com.marketingagencybackend.enums.SubscriptionPlan;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SubscriptionPurchaseResponseDTO(
    Long id,
    Long clientId,
    String clientName,
    String companyName,
    SubscriptionPlan plan,
    BillingBasis billingBasis,
    Double price,
    SubscriptionApprovalStatus status,
    String adminNote,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
