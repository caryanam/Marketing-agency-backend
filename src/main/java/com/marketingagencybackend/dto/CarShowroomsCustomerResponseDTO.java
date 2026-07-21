package com.marketingagencybackend.dto;

import com.marketingagencybackend.entity.CarShowroomsCustomer;

import java.time.LocalDateTime;

public record CarShowroomsCustomerResponseDTO(

        Long id,
        String name,
        String whatsappNumber,
        LocalDateTime createdAt

) {

    public static CarShowroomsCustomerResponseDTO from(CarShowroomsCustomer customer) {

        return new CarShowroomsCustomerResponseDTO(
                customer.getId(),
                customer.getName(),
                customer.getWhatsappNumber(),
                customer.getCreatedAt()
        );
    }
}
