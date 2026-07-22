package com.marketingagencybackend.service;

import com.marketingagencybackend.dto.request.CampaignRequestDTO;
import com.marketingagencybackend.dto.response.CampaignResponseDTO;

import java.util.List;

public interface CampaignService {
    
    CampaignResponseDTO createCampaign(CampaignRequestDTO request);
    
    CampaignResponseDTO runCampaign(Long campaignId, Integer messagesToSend);
    
    CampaignResponseDTO pauseCampaign(Long campaignId);
    
    CampaignResponseDTO resumeCampaign(Long campaignId);
    
    CampaignResponseDTO stopCampaign(Long campaignId);
    
    List<CampaignResponseDTO> getAllCampaigns();
    
    List<CampaignResponseDTO> getCampaignsByClient(Long clientId);
}
