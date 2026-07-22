package com.marketingagencybackend.service.serviceImpl;

import com.marketingagencybackend.dto.response.AdminAnalyticsResponseDTO;
import com.marketingagencybackend.entity.Campaign;
import com.marketingagencybackend.enums.PaymentStatus;
import com.marketingagencybackend.enums.SubscriptionStatus;
import com.marketingagencybackend.repository.CampaignRepository;
import com.marketingagencybackend.repository.ClientSubscriptionRepository;
import com.marketingagencybackend.repository.PaymentHistoryRepository;
import com.marketingagencybackend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ClientSubscriptionRepository clientSubscriptionRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final CampaignRepository campaignRepository;

    @Override
    public AdminAnalyticsResponseDTO getAdminAnalytics() {
        AdminAnalyticsResponseDTO analytics = new AdminAnalyticsResponseDTO();
        
        long activeCount = clientSubscriptionRepository.findAll().stream()
                .filter(sub -> sub.getSubscriptionStatus() == SubscriptionStatus.ACTIVE)
                .count();
        analytics.setTotalActiveSubscriptions(activeCount);
        
        long expiredCount = clientSubscriptionRepository.findAll().stream()
                .filter(sub -> sub.getSubscriptionStatus() == SubscriptionStatus.EXPIRED)
                .count();
        analytics.setTotalExpiredSubscriptions(expiredCount);
        
        double revenue = paymentHistoryRepository.findAll().stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.APPROVED)
                .mapToDouble(payment -> payment.getAmount() != null ? payment.getAmount() : 0.0)
                .sum();
        analytics.setTotalRevenue(revenue);
        
        long pending = paymentHistoryRepository.findAll().stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.PENDING)
                .count();
        analytics.setPendingPayments(pending);
        
        List<Campaign> campaigns = campaignRepository.findAll();
        analytics.setTotalCampaignsRun((long) campaigns.size());
        
        long messagesSent = campaigns.stream()
                .mapToLong(c -> c.getMessagesSent() != null ? c.getMessagesSent() : 0)
                .sum();
        analytics.setTotalMessagesSent(messagesSent);
        
        return analytics;
    }
}
