package com.marketingagencybackend.dto.response;

import com.marketingagencybackend.enums.PaymentStatus;
import com.marketingagencybackend.enums.SubscriptionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientSubscriptionResponseDTO {
    private Long id;
    private Long clientId;
    private PlanResponseDTO plan;
    private PaymentStatus paymentStatus;
    private SubscriptionStatus subscriptionStatus;
    private Double amount;
    private LocalDateTime purchaseDate;
    private LocalDateTime approvedDate;
    private LocalDateTime expiryDate;
    private Integer remainingMessages;
    private Integer campaignUsed;
}
