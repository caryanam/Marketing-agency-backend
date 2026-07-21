package com.marketingagencybackend.dto;

import com.marketingagencybackend.enums.BusinessCategory;
import com.marketingagencybackend.enums.FeedbackStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record FeedbackResponseDTO(
    Long id,
    Long clientId,
    String clientName,
    String companyName,
    String designation,
    BusinessCategory serviceName,
    Integer rating,
    String comment,
    FeedbackStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
