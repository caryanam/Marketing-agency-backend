package com.marketingagencybackend.dto.response;

import com.marketingagencybackend.enums.PlanCode;
import com.marketingagencybackend.enums.PlanType;
import lombok.Data;

@Data
public class PlanResponseDTO {
    private Long id;
    private String planName;
    private PlanCode planCode;
    private PlanType planType;
    private Double price;
    private Integer messageLimit;
    private Integer campaignLimit;
    private Integer validityDays;
    private Boolean isActive;
}
