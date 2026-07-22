package com.marketingagencybackend.entity;

import lombok.*;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerData {

    private Long id;
    private String customerName;

    private String whatsappNumber;
    private Long clientId;
    private com.marketingagencybackend.enums.BusinessCategory businessCategory;

    private LocalDateTime createdAt;
}
