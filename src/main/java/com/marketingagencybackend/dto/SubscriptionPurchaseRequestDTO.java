package com.marketingagencybackend.dto;

import com.marketingagencybackend.enums.BillingBasis;
import com.marketingagencybackend.enums.DailySubscriptionPlan;
import com.marketingagencybackend.enums.MonthlySubscriptionPlan;
import com.marketingagencybackend.enums.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record SubscriptionPurchaseRequestDTO(
    @NotNull(message = "Client ID is required")
    Long clientId,

    @NotNull(message = "Billing basis is required (DAILY or MONTHLY)")
    BillingBasis billingBasis,

    DailySubscriptionPlan dailyPlan,

    MonthlySubscriptionPlan monthlyPlan,

    SubscriptionPlan plan
) {}
