package com.marketingagencybackend.service;

import com.marketingagencybackend.dto.request.PaymentApprovalRequestDTO;
import com.marketingagencybackend.dto.request.PurchaseSubscriptionRequestDTO;
import com.marketingagencybackend.dto.response.ClientSubscriptionResponseDTO;
import com.marketingagencybackend.dto.response.PaymentHistoryResponseDTO;
import com.marketingagencybackend.dto.response.SubscriptionUsageResponseDTO;

import java.util.List;

public interface ClientSubscriptionService {
    
    // Client Methods
    ClientSubscriptionResponseDTO purchaseSubscription(Long clientId, PurchaseSubscriptionRequestDTO request);
    
    ClientSubscriptionResponseDTO upgradeSubscription(Long clientId, PurchaseSubscriptionRequestDTO request);
    
    ClientSubscriptionResponseDTO getCurrentSubscription(Long clientId);
    
    List<ClientSubscriptionResponseDTO> getSubscriptionHistory(Long clientId);
    
    SubscriptionUsageResponseDTO getSubscriptionUsage(Long clientId);
    
    List<PaymentHistoryResponseDTO> getPaymentHistory(Long clientId);
    
    
    // Admin Methods
    List<ClientSubscriptionResponseDTO> getAllSubscriptions();
    
    List<PaymentHistoryResponseDTO> getAllPayments();
    
    List<PaymentHistoryResponseDTO> getPendingPayments();
    
    ClientSubscriptionResponseDTO approveOrRejectPayment(Long paymentId, String adminEmail, PaymentApprovalRequestDTO request);
    
}
