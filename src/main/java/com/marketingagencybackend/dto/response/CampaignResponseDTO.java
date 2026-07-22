package com.marketingagencybackend.dto.response;

import com.marketingagencybackend.enums.CampaignStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CampaignResponseDTO {
    private Long id;
    private Long clientId;
    private Long subscriptionId;
    private String campaignName;
    private CampaignStatus campaignStatus;
    private Integer messagesSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
