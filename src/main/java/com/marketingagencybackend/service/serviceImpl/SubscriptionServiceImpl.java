package com.marketingagencybackend.service.serviceImpl;

import com.marketingagencybackend.dto.SubscriptionApprovalRequestDTO;
import com.marketingagencybackend.dto.SubscriptionPurchaseRequestDTO;
import com.marketingagencybackend.dto.SubscriptionPurchaseResponseDTO;
import com.marketingagencybackend.entity.Client;
import com.marketingagencybackend.entity.SubscriptionPurchase;
import com.marketingagencybackend.enums.BillingBasis;
import com.marketingagencybackend.enums.DailySubscriptionPlan;
import com.marketingagencybackend.enums.MonthlySubscriptionPlan;
import com.marketingagencybackend.enums.SubscriptionApprovalStatus;
import com.marketingagencybackend.enums.SubscriptionPlan;
import com.marketingagencybackend.exception.ResourceNotFoundException;
import com.marketingagencybackend.repository.ClientRepository;
import com.marketingagencybackend.repository.SubscriptionPurchaseRepository;
import com.marketingagencybackend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionPurchaseRepository subscriptionPurchaseRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public SubscriptionPurchaseResponseDTO purchasePlan(SubscriptionPurchaseRequestDTO requestDTO) {
        log.info("Client ID {} requesting subscription purchase for billingBasis: {}",
                requestDTO.clientId(), requestDTO.billingBasis());

        Client client = clientRepository.findById(requestDTO.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + requestDTO.clientId()));

        SubscriptionPlan finalPlan = resolveSubscriptionPlan(requestDTO);
        Double calculatedPrice = calculatePrice(finalPlan, requestDTO.billingBasis());

        SubscriptionPurchase purchase = SubscriptionPurchase.builder()
                .client(client)
                .plan(finalPlan)
                .billingBasis(requestDTO.billingBasis())
                .price(calculatedPrice)
                .status(SubscriptionApprovalStatus.PENDING)
                .build();

        SubscriptionPurchase savedPurchase = subscriptionPurchaseRepository.save(purchase);
        log.info("Saved new subscription purchase request id {} for client id {}", savedPurchase.getId(), client.getId());

        return mapToResponseDTO(savedPurchase);
    }

    @Override
    @Transactional
    public SubscriptionPurchaseResponseDTO approveOrRejectPurchase(SubscriptionApprovalRequestDTO requestDTO) {
        log.info("Admin approving/rejecting purchase request id {} with status {}",
                requestDTO.purchaseId(), requestDTO.status());

        SubscriptionPurchase purchase = subscriptionPurchaseRepository.findById(requestDTO.purchaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Subscription purchase request not found with id: " + requestDTO.purchaseId()));

        purchase.setStatus(requestDTO.status());
        if (requestDTO.adminNote() != null) {
            purchase.setAdminNote(requestDTO.adminNote());
        }

        SubscriptionPurchase updatedPurchase = subscriptionPurchaseRepository.save(purchase);
        return mapToResponseDTO(updatedPurchase);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPurchaseResponseDTO> getAllPurchases() {
        return subscriptionPurchaseRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPurchaseResponseDTO> getPurchasesByClientId(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found with id: " + clientId);
        }
        return subscriptionPurchaseRepository.findByClientId(clientId).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPurchaseResponseDTO> getPurchasesByStatus(SubscriptionApprovalStatus status) {
        return subscriptionPurchaseRepository.findByStatus(status).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private SubscriptionPlan resolveSubscriptionPlan(SubscriptionPurchaseRequestDTO requestDTO) {
        if (requestDTO.billingBasis() == BillingBasis.DAILY) {
            if (requestDTO.dailyPlan() != null) {
                switch (requestDTO.dailyPlan()) {
                    case STARTER_DAILY: return SubscriptionPlan.STARTER_DAILY;
                    case GROWTH_DAILY: return SubscriptionPlan.GROWTH_DAILY;
                    case ENTERPRISE_DAILY: return SubscriptionPlan.ENTERPRISE_DAILY;
                }
            }
        } else if (requestDTO.billingBasis() == BillingBasis.MONTHLY) {
            if (requestDTO.monthlyPlan() != null) {
                switch (requestDTO.monthlyPlan()) {
                    case STARTER_MONTHLY: return SubscriptionPlan.STARTER_MONTHLY;
                    case GROWTH_MONTHLY: return SubscriptionPlan.GROWTH_MONTHLY;
                    case ENTERPRISE_MONTHLY: return SubscriptionPlan.ENTERPRISE_MONTHLY;
                }
            }
        }

        if (requestDTO.plan() != null) {
            return requestDTO.plan();
        }

        throw new IllegalArgumentException("Please select a valid plan for " + requestDTO.billingBasis() + " subscription.");
    }

    private Double calculatePrice(SubscriptionPlan plan, BillingBasis billingBasis) {
        if (plan == SubscriptionPlan.STARTER_DAILY) {
            return 499.0;
        } else if (plan == SubscriptionPlan.GROWTH_DAILY) {
            return 1199.0;
        } else if (plan == SubscriptionPlan.STARTER_MONTHLY) {
            return 14999.0;
        } else if (plan == SubscriptionPlan.GROWTH_MONTHLY) {
            return 34999.0;
        } else if (plan == SubscriptionPlan.ENTERPRISE_DAILY || plan == SubscriptionPlan.ENTERPRISE_MONTHLY) {
            return 0.0; // Custom enterprise pricing
        }
        return 0.0;
    }

    private SubscriptionPurchaseResponseDTO mapToResponseDTO(SubscriptionPurchase purchase) {
        return SubscriptionPurchaseResponseDTO.builder()
                .id(purchase.getId())
                .clientId(purchase.getClient().getId())
                .clientName(purchase.getClient().getOwnerName())
                .companyName(purchase.getClient().getCompanyName())
                .plan(purchase.getPlan())
                .billingBasis(purchase.getBillingBasis())
                .price(purchase.getPrice())
                .status(purchase.getStatus())
                .adminNote(purchase.getAdminNote())
                .createdAt(purchase.getCreatedAt())
                .updatedAt(purchase.getUpdatedAt())
                .build();
    }
}
