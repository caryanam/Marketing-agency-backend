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
    private final com.marketingagencybackend.repository.CustomerDataRepository customerDataRepository;
    private final com.marketingagencybackend.repository.MessageLogRepository messageLogRepository;
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

        return mapToDTO(savedCampaign);
    }

    @Override
    @Transactional
    public CampaignResponseDTO runCampaign(Long campaignId, Integer messagesToSend) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found"));
                
        if (campaign.getCampaignStatus() == CampaignStatus.COMPLETED) {
            throw new CampaignException("Cannot run a campaign that is " + campaign.getCampaignStatus());
        }
        
        ClientSubscription activeSub = campaign.getSubscription();
        
        if (activeSub.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new SubscriptionException("Subscription has expired. Cannot run campaign.");
        }
        
        if (activeSub.getRemainingMessages() < messagesToSend) {
            throw new CampaignException("Message Limit Exceeded. Remaining: " + activeSub.getRemainingMessages() + ", Requested: " + messagesToSend);
        }

        // Fetch uncontacted customers
        List<com.marketingagencybackend.entity.CustomerData> uncontactedCustomers = 
            customerDataRepository.findUncontactedCustomers(
                campaign.getClient().getId(), 
                org.springframework.data.domain.PageRequest.of(0, messagesToSend)
            );

        if (uncontactedCustomers.isEmpty()) {
            throw new CampaignException("No new uncontacted customers available for this client.");
        }

        int actualSent = uncontactedCustomers.size();
        
        // Execute run
        campaign.setCampaignStatus(CampaignStatus.RUNNING);
        campaign.setMessagesSent(campaign.getMessagesSent() + actualSent);
        
        // Deduct messages
        activeSub.setRemainingMessages(activeSub.getRemainingMessages() - actualSent);

        // Save Message Logs
        List<com.marketingagencybackend.entity.MessageLog> logs = new java.util.ArrayList<>();
        for (com.marketingagencybackend.entity.CustomerData customer : uncontactedCustomers) {
            logs.add(com.marketingagencybackend.entity.MessageLog.builder()
                    .client(campaign.getClient())
                    .customer(customer)
                    .campaign(campaign)
                    .status("SENT")
                    .build());
        }
        messageLogRepository.saveAll(logs);
        
        campaignRepository.save(campaign);
        subscriptionRepository.save(activeSub);
        
        notificationService.sendNotification(activeSub.getClient().getEmail(), "Campaign Running", 
                "Your campaign '" + campaign.getCampaignName() + "' is running. " + actualSent + " messages sent.", com.marketingagencybackend.enums.NotificationType.CAMPAIGN);

        return mapToDTO(campaign);
    }



    @Override
    public List<CampaignResponseDTO> getAllCampaigns() {
        return campaignRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CampaignResponseDTO> getCampaignsByClient(Long clientId) {
        return campaignRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    private CampaignResponseDTO mapToDTO(Campaign campaign) {
        CampaignResponseDTO dto = modelMapper.map(campaign, CampaignResponseDTO.class);
        if (campaign.getSubscription() != null) {
            dto.setRemainingMessages(campaign.getSubscription().getRemainingMessages());
        }
        return dto;
    }
}
