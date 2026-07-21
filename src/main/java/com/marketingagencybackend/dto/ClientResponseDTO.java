package com.marketingagencybackend.dto;

import com.marketingagencybackend.entity.Client;
import com.marketingagencybackend.enums.BillingBasis;
import com.marketingagencybackend.enums.BusinessCategory;
import com.marketingagencybackend.enums.SubscriptionPlan;

import java.time.LocalDateTime;

public record ClientResponseDTO(

        Long id,
        String ownerName,
        String companyName,
        BusinessCategory category,
        String phoneNumber,
        String whatsappNumber,
        String email,
        SubscriptionPlan subscriptionPlan,
        BillingBasis billingBasis,
        LocalDateTime createdAt

) {

    public static ClientResponseDTO from(Client client) {

        return new ClientResponseDTO(

                client.getId(),
                client.getOwnerName(),
                client.getCompanyName(),
                client.getCategory(),
                client.getPhoneNumber(),
                client.getWhatsappNumber(),
                client.getEmail(),
                client.getSubscriptionPlan(),
                client.getBillingBasis(),
                client.getCreatedAt()
        );
    }
}
