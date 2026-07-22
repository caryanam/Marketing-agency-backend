package com.marketingagencybackend.controller;

import com.marketingagencybackend.dto.ApiResponseDTO;
import com.marketingagencybackend.dto.request.PurchaseSubscriptionRequestDTO;
import com.marketingagencybackend.dto.response.ClientSubscriptionResponseDTO;
import com.marketingagencybackend.dto.response.PaymentHistoryResponseDTO;
import com.marketingagencybackend.dto.response.PlanResponseDTO;
import com.marketingagencybackend.dto.response.SubscriptionUsageResponseDTO;
import com.marketingagencybackend.security.CustomUserDetails;
import com.marketingagencybackend.service.ClientSubscriptionService;
import com.marketingagencybackend.service.SubscriptionPlanService;
import com.marketingagencybackend.service.CampaignService;
import com.marketingagencybackend.dto.response.CampaignResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Client Subscription & Billing", description = "Client endpoints for purchasing plans and viewing usage")
@PreAuthorize("hasRole('CLIENT')")
public class ClientSubscriptionController {

    private final SubscriptionPlanService planService;
    private final ClientSubscriptionService subscriptionService;
    private final CampaignService campaignService;

    // --- PLANS (View Only) ---

    @GetMapping("/plans")
    @Operation(summary = "Get all active subscription plans")
    public ResponseEntity<ApiResponseDTO<List<PlanResponseDTO>>> getActivePlans() {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Active plans fetched", planService.getActivePlans()));
    }

    @GetMapping("/plans/{id}")
    @Operation(summary = "Get a specific plan by ID")
    public ResponseEntity<ApiResponseDTO<PlanResponseDTO>> getPlanById(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Plan fetched", planService.getPlanById(id)));
    }

    // --- PURCHASING & UPGRADES ---

    @PostMapping("/subscription/purchase")
    @Operation(summary = "Purchase a new subscription plan")
    public ResponseEntity<ApiResponseDTO<ClientSubscriptionResponseDTO>> purchaseSubscription(
            @AuthenticationPrincipal CustomUserDetails client,
            @Valid @RequestBody PurchaseSubscriptionRequestDTO request) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Subscription purchase requested successfully. Awaiting admin approval.",
                subscriptionService.purchaseSubscription(client.getId(), request)));
    }

    @PostMapping("/subscription/upgrade")
    @Operation(summary = "Upgrade an existing active subscription")
    public ResponseEntity<ApiResponseDTO<ClientSubscriptionResponseDTO>> upgradeSubscription(
            @AuthenticationPrincipal CustomUserDetails client,
            @Valid @RequestBody PurchaseSubscriptionRequestDTO request) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Subscription upgrade requested successfully. Awaiting admin approval.",
                subscriptionService.upgradeSubscription(client.getId(), request)));
    }

    // --- USAGE & HISTORY ---

    @GetMapping("/subscription/current")
    @Operation(summary = "Get current active subscription details")
    public ResponseEntity<ApiResponseDTO<ClientSubscriptionResponseDTO>> getCurrentSubscription(
            @AuthenticationPrincipal CustomUserDetails client) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Current subscription fetched",
                subscriptionService.getCurrentSubscription(client.getId())));
    }

    @GetMapping("/subscription/history")
    @Operation(summary = "Get all past and current subscriptions")
    public ResponseEntity<ApiResponseDTO<List<ClientSubscriptionResponseDTO>>> getSubscriptionHistory(
            @AuthenticationPrincipal CustomUserDetails client) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Subscription history fetched",
                subscriptionService.getSubscriptionHistory(client.getId())));
    }

    @GetMapping("/subscription/usage")
    @Operation(summary = "Get current usage metrics (messages and campaigns)")
    public ResponseEntity<ApiResponseDTO<SubscriptionUsageResponseDTO>> getSubscriptionUsage(
            @AuthenticationPrincipal CustomUserDetails client,
            @RequestParam(required = false) Long clientId) {
        Long targetId = (clientId != null && client.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) ? clientId : client.getId();
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Subscription usage fetched",
                subscriptionService.getSubscriptionUsage(targetId)));
    }

    @GetMapping("/payment/history")
    @Operation(summary = "Get payment history")
    public ResponseEntity<ApiResponseDTO<List<PaymentHistoryResponseDTO>>> getPaymentHistory(
            @AuthenticationPrincipal CustomUserDetails client) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Payment history fetched",
                subscriptionService.getPaymentHistory(client.getId())));
    }

    @GetMapping("/subscription/campaigns")
    @Operation(summary = "Get current client's campaigns")
    public ResponseEntity<ApiResponseDTO<List<CampaignResponseDTO>>> getClientCampaigns(
            @AuthenticationPrincipal CustomUserDetails client,
            @RequestParam(required = false) Long clientId) {
        Long targetId = (clientId != null && client.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) ? clientId : client.getId();
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Campaigns fetched successfully",
                campaignService.getCampaignsByClient(targetId)));
    }
}
