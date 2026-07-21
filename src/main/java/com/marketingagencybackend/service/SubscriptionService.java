package com.marketingagencybackend.service;

import com.marketingagencybackend.dto.SubscriptionApprovalRequestDTO;
import com.marketingagencybackend.dto.SubscriptionPurchaseRequestDTO;
import com.marketingagencybackend.dto.SubscriptionPurchaseResponseDTO;
import com.marketingagencybackend.enums.SubscriptionApprovalStatus;

import java.util.List;

public interface SubscriptionService {

    SubscriptionPurchaseResponseDTO purchasePlan(SubscriptionPurchaseRequestDTO requestDTO);

    SubscriptionPurchaseResponseDTO approveOrRejectPurchase(SubscriptionApprovalRequestDTO requestDTO);

    List<SubscriptionPurchaseResponseDTO> getAllPurchases();

    List<SubscriptionPurchaseResponseDTO> getPurchasesByClientId(Long clientId);

    List<SubscriptionPurchaseResponseDTO> getPurchasesByStatus(SubscriptionApprovalStatus status);
}
