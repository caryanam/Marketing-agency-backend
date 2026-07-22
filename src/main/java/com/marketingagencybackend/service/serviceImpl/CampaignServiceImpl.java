package com.marketingagencybackend.service.serviceImpl;

import com.marketingagencybackend.dto.request.CampaignRequestDTO;
import com.marketingagencybackend.dto.response.CampaignResponseDTO;
import com.marketingagencybackend.entity.Campaign;
import com.marketingagencybackend.entity.Client;
import com.marketingagencybackend.entity.ClientSubscription;
import com.marketingagencybackend.entity.SubscriptionPlan;
import com.marketingagencybackend.enums.CampaignStatus;
import com.marketingagencybackend.enums.SubscriptionStatus;
import com.marketingagencybackend.exception.CampaignException;
import com.marketingagencybackend.exception.ResourceNotFoundException;
import com.marketingagencybackend.exception.SubscriptionException;
import com.marketingagencybackend.repository.CampaignRepository;
import com.marketingagencybackend.repository.ClientRepository;
import com.marketingagencybackend.repository.ClientSubscriptionRepository;
import com.marketingagencybackend.service.CampaignService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignServiceImpl implements CampaignService {

    private final CampaignRepository campaignRepository;
    private final ClientSubscriptionRepository clientSubscriptionRepository;
    private final ClientRepository clientRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CampaignResponseDTO createCampaign(CampaignRequestDTO request) {
        
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
                
        // Check for active subscription
        ClientSubscription activeSub = clientSubscriptionRepository.findByClientIdAndSubscriptionStatus(client.getId(), SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionException("Client does not have an active subscription"));
                
        // Check if subscription has expired
        if (activeSub.getExpiryDate().isBefore(LocalDateTime.now())) {
            activeSub.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            clientSubscriptionRepository.save(activeSub);
            throw new SubscriptionException("Subscription has expired. Cannot create new campaigns.");
        }
        
        SubscriptionPlan plan = activeSub.getSubscriptionPlan();
        
        // Validate Campaign Limits
        if (activeSub.getCampaignUsed() >= plan.getCampaignLimit()) {
            throw new CampaignException("Campaign Limit Exceeded. You have used " + activeSub.getCampaignUsed() + " out of " + plan.getCampaignLimit() + " allowed campaigns.");
        }

        // Create campaign
        Campaign campaign = new Campaign();
        campaign.setClient(client);
        campaign.setSubscription(activeSub);
        campaign.setCampaignName(request.getCampaignName());
        campaign.setCampaignStatus(CampaignStatus.CREATED);
        campaign.setMessagesSent(0);
        
        Campaign savedCampaign = campaignRepository.save(campaign);
        
        // Update subscription campaign count
        activeSub.setCampaignUsed(activeSub.getCampaignUsed() + 1);
        clientSubscriptionRepository.save(activeSub);

        return modelMapper.map(savedCampaign, CampaignResponseDTO.class);
    }

    @Override
    @Transactional
    public CampaignResponseDTO runCampaign(Long campaignId, Integer messagesToSend) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
                
        if (campaign.getCampaignStatus() == CampaignStatus.COMPLETED || campaign.getCampaignStatus() == CampaignStatus.STOPPED) {
            throw new CampaignException("Cannot run a campaign that is already " + campaign.getCampaignStatus());
        }
        
        ClientSubscription activeSub = campaign.getSubscription();
        
        if (activeSub.getSubscriptionStatus() != SubscriptionStatus.ACTIVE) {
            throw new SubscriptionException("Subscription is not active (Status: " + activeSub.getSubscriptionStatus() + ")");
        }
        
        if (activeSub.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new SubscriptionException("Subscription has expired");
        }
        
        // Validate Message Limit
        if (activeSub.getRemainingMessages() < messagesToSend) {
            throw new CampaignException("Message Limit Exceeded. Remaining: " + activeSub.getRemainingMessages() + ", Requested: " + messagesToSend);
        }
        
        // Update campaign
        campaign.setCampaignStatus(CampaignStatus.RUNNING);
        campaign.setMessagesSent(campaign.getMessagesSent() + messagesToSend);
        Campaign updatedCampaign = campaignRepository.save(campaign);
        
        // Decrease remaining messages
        activeSub.setRemainingMessages(activeSub.getRemainingMessages() - messagesToSend);
        clientSubscriptionRepository.save(activeSub);
        
        return modelMapper.map(updatedCampaign, CampaignResponseDTO.class);
    }

    @Override
    @Transactional
    public CampaignResponseDTO pauseCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
                
        if (campaign.getCampaignStatus() != CampaignStatus.RUNNING) {
            throw new CampaignException("Only running campaigns can be paused");
        }
        
        campaign.setCampaignStatus(CampaignStatus.PAUSED);
        return modelMapper.map(campaignRepository.save(campaign), CampaignResponseDTO.class);
    }

    @Override
    @Transactional
    public CampaignResponseDTO resumeCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
                
        if (campaign.getCampaignStatus() != CampaignStatus.PAUSED) {
            throw new CampaignException("Only paused campaigns can be resumed");
        }
        
        ClientSubscription activeSub = campaign.getSubscription();
        if (activeSub.getSubscriptionStatus() != SubscriptionStatus.ACTIVE) {
            throw new SubscriptionException("Subscription is not active");
        }
        
        campaign.setCampaignStatus(CampaignStatus.RUNNING);
        return modelMapper.map(campaignRepository.save(campaign), CampaignResponseDTO.class);
    }

    @Override
    @Transactional
    public CampaignResponseDTO stopCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
                
        if (campaign.getCampaignStatus() == CampaignStatus.COMPLETED) {
            throw new CampaignException("Campaign is already completed");
        }
        
        campaign.setCampaignStatus(CampaignStatus.STOPPED);
        return modelMapper.map(campaignRepository.save(campaign), CampaignResponseDTO.class);
    }

    @Override
    public List<CampaignResponseDTO> getAllCampaigns() {
        return campaignRepository.findAll().stream()
                .map(c -> modelMapper.map(c, CampaignResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<CampaignResponseDTO> getCampaignsByClient(Long clientId) {
        return campaignRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(c -> modelMapper.map(c, CampaignResponseDTO.class))
                .collect(Collectors.toList());
    }
}
