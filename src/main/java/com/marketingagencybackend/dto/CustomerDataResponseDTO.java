package com.marketingagencybackend.dto;

import com.marketingagencybackend.entity.CustomerData;

import java.time.LocalDateTime;

public record CustomerDataResponseDTO(

        Long id,
        String customerName,
        String whatsappNumber,
        LocalDateTime createdAt

) {

    public static CustomerDataResponseDTO from(CustomerData customerData) {

        return new CustomerDataResponseDTO(
                customerData.getId(),
                customerData.getCustomerName(),
                customerData.getWhatsappNumber(),
                customerData.getCreatedAt()
        );
    }
}
