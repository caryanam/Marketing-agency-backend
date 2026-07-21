package com.marketingagencybackend.dto;

import com.marketingagencybackend.enums.FeedbackStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record FeedbackApprovalRequestDTO(
    @NotNull(message = "Feedback ID is required")
    Long feedbackId,

    @NotNull(message = "Status is required (APPROVED or REJECTED)")
    FeedbackStatus status
) {}
