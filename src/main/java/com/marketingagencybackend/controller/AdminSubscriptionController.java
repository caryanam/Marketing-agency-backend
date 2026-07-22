package com.marketingagencybackend.controller;

import com.marketingagencybackend.dto.ApiResponseDTO;
import com.marketingagencybackend.dto.request.CampaignRequestDTO;
import com.marketingagencybackend.dto.request.PaymentApprovalRequestDTO;
import com.marketingagencybackend.dto.request.PlanRequestDTO;
import com.marketingagencybackend.dto.response.*;
import com.marketingagencybackend.security.CustomUserDetails;
import com.marketingagencybackend.service.AnalyticsService;
import com.marketingagencybackend.service.CampaignService;
import com.marketingagencybackend.service.ClientSubscriptionService;
import com.marketingagencybackend.service.SubscriptionPlanService;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Subscription & Billing", description = "Admin endpoints for managing plans, payments, campaigns, and analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSubscriptionController {

    private final SubscriptionPlanService planService;
    private final ClientSubscriptionService subscriptionService;
    private final CampaignService campaignService;
    private final AnalyticsService analyticsService;

    // --- PLANS ---
    
    @PostMapping("/plans")
    @Operation(summary = "Create a new subscription plan")
    public ResponseEntity<ApiResponseDTO<PlanResponseDTO>> createPlan(@Valid @RequestBody PlanRequestDTO request) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Plan created successfully", planService.createPlan(request)));
    }

    @PutMapping("/plans/{id}")
    @Operation(summary = "Update an existing plan")
    public ResponseEntity<ApiResponseDTO<PlanResponseDTO>> updatePlan(@PathVariable Long id, @Valid @RequestBody PlanRequestDTO request) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Plan updated successfully", planService.updatePlan(id, request)));
    }

    @DeleteMapping("/plans/{id}")
    @Operation(summary = "Disable a plan")
    public ResponseEntity<ApiResponseDTO<Object>> disablePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Plan disabled successfully", null));
    }

    @GetMapping("/plans")
    @Operation(summary = "Get all plans (including disabled)")
    public ResponseEntity<ApiResponseDTO<List<PlanResponseDTO>>> getAllPlans() {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Plans fetched successfully", planService.getAllPlans()));
    }

    // --- SUBSCRIPTIONS & PAYMENTS ---
    
    @GetMapping("/subscriptions")
    @Operation(summary = "Get all client subscriptions")
    public ResponseEntity<ApiResponseDTO<List<ClientSubscriptionResponseDTO>>> getAllSubscriptions() {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Subscriptions fetched successfully", subscriptionService.getAllSubscriptions()));
    }

    @GetMapping("/payments")
    @Operation(summary = "Get all payments history")
    public ResponseEntity<ApiResponseDTO<List<PaymentHistoryResponseDTO>>> getAllPayments() {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Payments fetched successfully", subscriptionService.getAllPayments()));
    }

    @GetMapping("/payment/pending")
    @Operation(summary = "Get all pending payments")
    public ResponseEntity<ApiResponseDTO<List<PaymentHistoryResponseDTO>>> getPendingPayments() {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Pending payments fetched", subscriptionService.getPendingPayments()));
    }

    @PatchMapping("/payment/{id}/approve-reject")
    @Operation(summary = "Approve or reject a pending payment")
    public ResponseEntity<ApiResponseDTO<ClientSubscriptionResponseDTO>> approveOrRejectPayment(
            @PathVariable Long id, 
            @AuthenticationPrincipal CustomUserDetails admin,
            @Valid @RequestBody PaymentApprovalRequestDTO request) {
        
        String message = request.getStatus().name().equals("APPROVED") 
                ? "Payment approved and subscription activated" 
                : "Payment rejected";
                
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", message, 
                subscriptionService.approveOrRejectPayment(id, admin.getUsername(), request)));
    }

    // --- CAMPAIGNS ---
    
    @PostMapping("/campaign")
    @Operation(summary = "Create a new campaign for a client")
    public ResponseEntity<ApiResponseDTO<CampaignResponseDTO>> createCampaign(@Valid @RequestBody CampaignRequestDTO request) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Campaign created successfully", campaignService.createCampaign(request)));
    }

    @PutMapping("/campaign/run/{id}")
    @Operation(summary = "Run a campaign (consumes messages)")
    public ResponseEntity<ApiResponseDTO<CampaignResponseDTO>> runCampaign(@PathVariable Long id, @RequestParam Integer messagesToSend) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Campaign is running", campaignService.runCampaign(id, messagesToSend)));
    }

    @PutMapping("/campaign/pause/{id}")
    @Operation(summary = "Pause a running campaign")
    public ResponseEntity<ApiResponseDTO<CampaignResponseDTO>> pauseCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Campaign paused", campaignService.pauseCampaign(id)));
    }

    @PutMapping("/campaign/resume/{id}")
    @Operation(summary = "Resume a paused campaign")
    public ResponseEntity<ApiResponseDTO<CampaignResponseDTO>> resumeCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Campaign resumed", campaignService.resumeCampaign(id)));
    }

    @PutMapping("/campaign/stop/{id}")
    @Operation(summary = "Stop a campaign completely")
    public ResponseEntity<ApiResponseDTO<CampaignResponseDTO>> stopCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Campaign stopped", campaignService.stopCampaign(id)));
    }

    // --- ANALYTICS ---
    
    @GetMapping("/analytics")
    @Operation(summary = "Get platform-wide analytics")
    public ResponseEntity<ApiResponseDTO<AdminAnalyticsResponseDTO>> getAnalytics() {
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Analytics fetched", analyticsService.getAdminAnalytics()));
    }
}
