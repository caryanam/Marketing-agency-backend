package com.marketingagencybackend.controller;

import com.marketingagencybackend.dto.ApiResponseDTO;
import com.marketingagencybackend.dto.request.MarkReadRequestDTO;
import com.marketingagencybackend.dto.response.NotificationResponseDTO;
import com.marketingagencybackend.security.CustomUserDetails;
import com.marketingagencybackend.service.NotificationService;
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
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for fetching and managing user notifications (Admin & Client)")
@PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get user notifications")
    public ResponseEntity<ApiResponseDTO<List<NotificationResponseDTO>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Notifications fetched", 
                notificationService.getUserNotifications(user.getUsername(), unreadOnly)));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponseDTO<Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails user) {
        
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Unread count fetched", 
                notificationService.getUnreadCount(user.getUsername())));
    }

    @PutMapping("/mark-read")
    @Operation(summary = "Mark specific notifications as read")
    public ResponseEntity<ApiResponseDTO<Object>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody MarkReadRequestDTO request) {
        
        notificationService.markAsRead(request.getNotificationIds(), user.getUsername());
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "Notifications marked as read", null));
    }

    @PutMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<ApiResponseDTO<Object>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails user) {
        
        notificationService.markAllAsRead(user.getUsername());
        return ResponseEntity.ok(new ApiResponseDTO<>("SUCCESS", "All notifications marked as read", null));
    }
}
