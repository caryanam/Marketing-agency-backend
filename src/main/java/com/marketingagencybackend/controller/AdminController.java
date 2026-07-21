package com.marketingagencybackend.controller;

import com.marketingagencybackend.dto.ApiResponseDTO;
import com.marketingagencybackend.dto.ExcelImportResponseDTO;
import com.marketingagencybackend.dto.FeedbackApprovalRequestDTO;
import com.marketingagencybackend.dto.FeedbackResponseDTO;
import com.marketingagencybackend.service.CustomerDataService;
import com.marketingagencybackend.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.marketingagencybackend.dto.SubscriptionApprovalRequestDTO;
import com.marketingagencybackend.dto.SubscriptionPurchaseResponseDTO;
import com.marketingagencybackend.enums.SubscriptionApprovalStatus;
import com.marketingagencybackend.service.SubscriptionService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Management", description = "Endpoints for Admin management operations")
public class AdminController {

    private final FeedbackService feedbackService;
    private final CustomerDataService customerDataService;
    private final SubscriptionService subscriptionService;

    @PostMapping(value = "/customer-data/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Import customer data from an excel sheet (.xlsx/.xls)", description = "Access Level: Protected [Required Role: ADMIN]")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Excel data imported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or empty file uploaded"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ApiResponseDTO<ExcelImportResponseDTO>> importCustomerData(
            @RequestParam("file") MultipartFile file) {

        log.info("Admin importing customer data from file: {}", file.getOriginalFilename());

        ExcelImportResponseDTO responseDTO = customerDataService.importFromExcel(file);

        return ResponseEntity.ok(
                ApiResponseDTO.<ExcelImportResponseDTO>builder()
                        .status("SUCCESS")
                        .message("Customer data imported successfully")
                        .data(responseDTO)
                        .build()
        );
    }

    @PatchMapping("/feedback/approve-reject")
    @Operation(summary = "Approve or Reject feedback", description = "Access Level: Protected [Required Role: ADMIN]")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feedback status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Feedback not found")
    })
    public ResponseEntity<ApiResponseDTO<FeedbackResponseDTO>> approveOrRejectFeedback(
            @Valid @RequestBody FeedbackApprovalRequestDTO requestDTO) {
        log.info("Admin updating feedback status for feedbackId: {} to {}", requestDTO.feedbackId(), requestDTO.status());
        FeedbackResponseDTO responseDTO = feedbackService.updateStatus(requestDTO.feedbackId(), requestDTO.status());

        return ResponseEntity.ok(
                ApiResponseDTO.<FeedbackResponseDTO>builder()
                        .status("SUCCESS")
                        .message("Feedback status updated successfully to " + requestDTO.status())
                        .data(responseDTO)
                        .build()
        );
    }

    @PatchMapping("/subscription/approve-reject")
    @Operation(summary = "Approve or Reject client subscription plan purchase", description = "Access Level: Protected [Required Role: ADMIN]")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subscription status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Subscription purchase request not found")
    })
    public ResponseEntity<ApiResponseDTO<SubscriptionPurchaseResponseDTO>> approveOrRejectSubscription(
            @Valid @RequestBody SubscriptionApprovalRequestDTO requestDTO) {
        log.info("Admin updating subscription status for purchaseId: {} to {}", requestDTO.purchaseId(), requestDTO.status());
        SubscriptionPurchaseResponseDTO responseDTO = subscriptionService.approveOrRejectPurchase(requestDTO);

        return ResponseEntity.ok(
                ApiResponseDTO.<SubscriptionPurchaseResponseDTO>builder()
                        .status("SUCCESS")
                        .message("Subscription purchase request updated successfully to " + requestDTO.status())
                        .data(responseDTO)
                        .build()
        );
    }

    @GetMapping("/subscription/all")
    @Operation(summary = "Get all client subscription purchase requests", description = "Access Level: Protected [Required Role: ADMIN]")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all subscription purchase requests"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ApiResponseDTO<List<SubscriptionPurchaseResponseDTO>>> getAllSubscriptions() {
        log.info("Admin fetching all subscription purchase requests");
        List<SubscriptionPurchaseResponseDTO> responseList = subscriptionService.getAllPurchases();

        return ResponseEntity.ok(
                ApiResponseDTO.<List<SubscriptionPurchaseResponseDTO>>builder()
                        .status("SUCCESS")
                        .message("All subscription purchase requests retrieved successfully")
                        .data(responseList)
                        .build()
        );
    }

    @GetMapping("/subscription/status/{status}")
    @Operation(summary = "Get subscription purchase requests by status (PENDING/APPROVED/REJECTED)", description = "Access Level: Protected [Required Role: ADMIN]")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved subscription purchase requests"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<ApiResponseDTO<List<SubscriptionPurchaseResponseDTO>>> getSubscriptionsByStatus(
            @PathVariable SubscriptionApprovalStatus status) {
        log.info("Admin fetching subscription purchase requests for status: {}", status);
        List<SubscriptionPurchaseResponseDTO> responseList = subscriptionService.getPurchasesByStatus(status);

        return ResponseEntity.ok(
                ApiResponseDTO.<List<SubscriptionPurchaseResponseDTO>>builder()
                        .status("SUCCESS")
                        .message("Subscription purchase requests retrieved successfully for status " + status)
                        .data(responseList)
                        .build()
        );
    }
}
