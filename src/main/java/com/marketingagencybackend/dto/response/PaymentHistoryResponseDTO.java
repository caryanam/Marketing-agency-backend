package com.marketingagencybackend.dto.response;

import com.marketingagencybackend.enums.PaymentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentHistoryResponseDTO {
    private Long id;
    private Long clientSubscriptionId;
    private Double amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String remarks;
    private LocalDateTime createdAt;
}
