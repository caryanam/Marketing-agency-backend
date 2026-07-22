package com.marketingagencybackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CampaignRequestDTO {
    
    @NotNull(message = "Client ID is required")
    private Long clientId;
    
    @NotBlank(message = "Campaign name is required")
    private String campaignName;
}
