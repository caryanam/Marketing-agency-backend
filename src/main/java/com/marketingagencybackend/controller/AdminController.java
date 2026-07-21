package com.marketingagencybackend.controller;

import com.marketingagencybackend.dto.ApiResponseDTO;
import com.marketingagencybackend.dto.FeedbackApprovalRequestDTO;
import com.marketingagencybackend.dto.FeedbackResponseDTO;
import com.marketingagencybackend.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Management", description = "Endpoints for Admin management operations")
public class AdminController {

    private final FeedbackService feedbackService;

    @PatchMapping("/feedback/approve-reject")
    @Operation(summary = "Approve or Reject feedback using request body containing feedbackId and status enum (APPROVED/REJECTED)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feedback status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
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

    @GetMapping("/feedback/{id}")
    @Operation(summary = "Get feedback details by feedback ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved feedback"),
            @ApiResponse(responseCode = "404", description = "Feedback not found")
    })
    public ResponseEntity<ApiResponseDTO<FeedbackResponseDTO>> getFeedbackById(@PathVariable Long id) {
        log.info("Admin fetching feedback by id: {}", id);
        FeedbackResponseDTO feedback = feedbackService.getFeedbackById(id);

        return ResponseEntity.ok(
                ApiResponseDTO.<FeedbackResponseDTO>builder()
                        .status("SUCCESS")
                        .message("Feedback retrieved successfully")
                        .data(feedback)
                        .build()
        );
    }

    @GetMapping("/feedback/all")
    @Operation(summary = "Get all feedback entries")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all feedback")
    public ResponseEntity<ApiResponseDTO<List<FeedbackResponseDTO>>> getAllFeedback() {
        log.info("Admin fetching all feedback");
        List<FeedbackResponseDTO> feedbackList = feedbackService.getAllFeedback();

        return ResponseEntity.ok(
                ApiResponseDTO.<List<FeedbackResponseDTO>>builder()
                        .status("SUCCESS")
                        .message("All feedback retrieved successfully")
                        .data(feedbackList)
                        .build()
        );
    }
}
