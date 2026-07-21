package com.marketingagencybackend.service;

import com.marketingagencybackend.dto.FeedbackRequestDTO;
import com.marketingagencybackend.dto.FeedbackResponseDTO;
import com.marketingagencybackend.enums.FeedbackStatus;

import java.util.List;

public interface FeedbackService {

    FeedbackResponseDTO createFeedback(Long clientId, FeedbackRequestDTO requestDTO);

    FeedbackResponseDTO updateFeedback(Long id, FeedbackRequestDTO requestDTO);

    FeedbackResponseDTO updateStatus(Long id, FeedbackStatus status);

    void deleteFeedback(Long id);

    FeedbackResponseDTO getFeedbackById(Long id);

    List<FeedbackResponseDTO> getAllFeedback();
}
