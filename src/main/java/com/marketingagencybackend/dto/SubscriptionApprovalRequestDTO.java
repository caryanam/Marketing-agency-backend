package com.marketingagencybackend.dto;

import com.marketingagencybackend.enums.SubscriptionApprovalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SubscriptionApprovalRequestDTO(
    @NotNull(message = "Purchase ID is required")
    Long purchaseId,

    @NotNull(message = "Approval status is required")
    SubscriptionApprovalStatus status,

    String adminNote
) {}
