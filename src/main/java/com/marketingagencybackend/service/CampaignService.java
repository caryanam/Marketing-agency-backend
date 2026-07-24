package com.marketingagencybackend.service;

import com.marketingagencybackend.dto.request.CampaignRequestDTO;
import com.marketingagencybackend.dto.response.CampaignResponseDTO;

import java.util.List;

public interface CampaignService {

    CampaignResponseDTO createCampaign(CampaignRequestDTO request);

    List<CampaignResponseDTO> getAllCampaigns();

    List<CampaignResponseDTO> getCampaignsByClient(Long clientId);
}
