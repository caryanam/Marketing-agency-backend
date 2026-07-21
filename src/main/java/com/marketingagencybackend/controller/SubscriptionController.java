package com.marketingagencybackend.controller;

import com.marketingagencybackend.dto.ApiResponseDTO;
import com.marketingagencybackend.dto.SubscriptionPurchaseRequestDTO;
import com.marketingagencybackend.dto.SubscriptionPurchaseResponseDTO;
import com.marketingagencybackend.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription Management", description = "Endpoints for Client Subscription Plan Purchasing and Upgrades")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/purchase")
    @Operation(summary = "Purchase or upgrade a subscription plan", description = "Access Level: Protected [Required Role: CLIENT]")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Subscription purchase request submitted successfully (Pending Admin Approval)"),
            @ApiResponse(responseCode = "400", description = "Invalid plan or billing basis input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid CLIENT JWT token required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - CLIENT role required"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ApiResponseDTO<SubscriptionPurchaseResponseDTO>> purchasePlan(
            @Valid @RequestBody SubscriptionPurchaseRequestDTO requestDTO) {
        log.info("Received subscription purchase request for client id: {}", requestDTO.clientId());
        SubscriptionPurchaseResponseDTO responseDTO = subscriptionService.purchasePlan(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.<SubscriptionPurchaseResponseDTO>builder()
                        .status("SUCCESS")
                        .message("Subscription plan purchase request submitted successfully. Awaiting admin approval.")
                        .data(responseDTO)
                        .build());
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get subscription purchase requests by Client ID", description = "Access Level: Protected [Required Role: CLIENT]")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved client subscription requests"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Valid JWT token required"),
            @ApiResponse(responseCode = "404", description = "Client not found")
    })
    public ResponseEntity<ApiResponseDTO<List<SubscriptionPurchaseResponseDTO>>> getPurchasesByClientId(
            @PathVariable Long clientId) {
        log.info("Fetching subscription purchases for client id: {}", clientId);
        List<SubscriptionPurchaseResponseDTO> responseList = subscriptionService.getPurchasesByClientId(clientId);

        return ResponseEntity.ok(
                ApiResponseDTO.<List<SubscriptionPurchaseResponseDTO>>builder()
                        .status("SUCCESS")
                        .message("Subscription purchase requests retrieved successfully")
                        .data(responseList)
                        .build()
        );
    }
}
