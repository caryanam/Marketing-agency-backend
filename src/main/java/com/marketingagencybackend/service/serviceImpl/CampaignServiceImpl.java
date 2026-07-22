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
import com.marketingagencybackend.service.NotificationService;
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
    private final ClientRepository clientRepository;
    private final ClientSubscriptionRepository subscriptionRepository;
    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public CampaignResponseDTO createCampaign(CampaignRequestDTO request) {
        
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
                
        ClientSubscription activeSub = subscriptionRepository.findByClientIdAndSubscriptionStatus(request.getClientId(), SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new SubscriptionException("Client does not have an active subscription"));
                
        if (activeSub.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new SubscriptionException("Subscription has expired. Cannot create campaign.");
        }
                
        if (activeSub.getCampaignUsed() >= activeSub.getSubscriptionPlan().getCampaignLimit()) {
            throw new CampaignException("Campaign Limit Exceeded. Maximum allowed: " + activeSub.getSubscriptionPlan().getCampaignLimit());
        }
        
        Campaign campaign = new Campaign();
        campaign.setClient(client);
        campaign.setSubscription(activeSub);
        campaign.setCampaignName(request.getCampaignName());
        campaign.setCampaignStatus(CampaignStatus.CREATED);
        campaign.setMessagesSent(0);
        
        Campaign savedCampaign = campaignRepository.save(campaign);
        
        // Update client usage
        activeSub.setCampaignUsed(activeSub.getCampaignUsed() + 1);
        subscriptionRepository.save(activeSub);
        
        notificationService.sendNotification(client.getEmail(), "New Campaign Created", 
                "A new campaign '" + request.getCampaignName() + "' has been created for your account.", com.marketingagencybackend.enums.NotificationType.CAMPAIGN);

        return modelMapper.map(savedCampaign, CampaignResponseDTO.class);
    }

    @Override
    @Transactional
    public CampaignResponseDTO runCampaign(Long campaignId, Integer messagesToSend) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
                
        if (campaign.getCampaignStatus() == CampaignStatus.COMPLETED || campaign.getCampaignStatus() == CampaignStatus.STOPPED) {
            throw new CampaignException("Cannot run a campaign that is " + campaign.getCampaignStatus());
        }
        
        ClientSubscription activeSub = campaign.getSubscription();
        
        if (activeSub.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new SubscriptionException("Subscription has expired. Cannot run campaign.");
        }
        
        if (activeSub.getRemainingMessages() < messagesToSend) {
            throw new CampaignException("Message Limit Exceeded. Remaining: " + activeSub.getRemainingMessages() + ", Requested: " + messagesToSend);
        }
        
        // Execute run
        campaign.setCampaignStatus(CampaignStatus.RUNNING);
        campaign.setMessagesSent(campaign.getMessagesSent() + messagesToSend);
        
        // Deduct messages
        activeSub.setRemainingMessages(activeSub.getRemainingMessages() - messagesToSend);
        
        campaignRepository.save(campaign);
        subscriptionRepository.save(activeSub);
        
        notificationService.sendNotification(activeSub.getClient().getEmail(), "Campaign Running", 
                "Your campaign '" + campaign.getCampaignName() + "' is running. " + messagesToSend + " messages sent.", com.marketingagencybackend.enums.NotificationType.CAMPAIGN);

        return modelMapper.map(campaign, CampaignResponseDTO.class);
    }

    @Override
    @Transactional
    public CampaignResponseDTO pauseCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
                
        if (campaign.getCampaignStatus() != CampaignStatus.RUNNING) {
            throw new CampaignException("Only RUNNING campaigns can be paused. Current status: " + campaign.getCampaignStatus());
        }
        
        campaign.setCampaignStatus(CampaignStatus.PAUSED);
        Campaign saved = campaignRepository.save(campaign);
        
        notificationService.sendNotification(campaign.getSubscription().getClient().getEmail(), "Campaign Paused", 
                "Your campaign '" + campaign.getCampaignName() + "' has been paused.", com.marketingagencybackend.enums.NotificationType.CAMPAIGN);
                
        return modelMapper.map(saved, CampaignResponseDTO.class);
    }

    @Override
    @Transactional
    public CampaignResponseDTO resumeCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
                
        if (campaign.getCampaignStatus() != CampaignStatus.PAUSED) {
            throw new CampaignException("Only PAUSED campaigns can be resumed. Current status: " + campaign.getCampaignStatus());
        }
        
        campaign.setCampaignStatus(CampaignStatus.RUNNING);
        Campaign saved = campaignRepository.save(campaign);
        
        notificationService.sendNotification(campaign.getSubscription().getClient().getEmail(), "Campaign Resumed", 
                "Your campaign '" + campaign.getCampaignName() + "' has been resumed.", com.marketingagencybackend.enums.NotificationType.CAMPAIGN);
                
        return modelMapper.map(saved, CampaignResponseDTO.class);
    }

    @Override
    @Transactional
    public CampaignResponseDTO stopCampaign(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
                
        if (campaign.getCampaignStatus() == CampaignStatus.COMPLETED || campaign.getCampaignStatus() == CampaignStatus.STOPPED) {
            throw new CampaignException("Campaign is already " + campaign.getCampaignStatus());
        }
        
        campaign.setCampaignStatus(CampaignStatus.STOPPED);
        Campaign saved = campaignRepository.save(campaign);
        
        notificationService.sendNotification(campaign.getSubscription().getClient().getEmail(), "Campaign Stopped", 
                "Your campaign '" + campaign.getCampaignName() + "' has been stopped by the Admin.", com.marketingagencybackend.enums.NotificationType.CAMPAIGN);
                
        return modelMapper.map(saved, CampaignResponseDTO.class);
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
