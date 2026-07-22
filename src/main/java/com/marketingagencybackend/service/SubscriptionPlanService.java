package com.marketingagencybackend.service;

import com.marketingagencybackend.dto.request.PlanRequestDTO;
import com.marketingagencybackend.dto.response.PlanResponseDTO;

import java.util.List;

public interface SubscriptionPlanService {
    
    PlanResponseDTO createPlan(PlanRequestDTO request);
    
    PlanResponseDTO updatePlan(Long id, PlanRequestDTO request);
    
    void deletePlan(Long id); // Actually sets isActive to false
    
    PlanResponseDTO getPlanById(Long id);
    
    List<PlanResponseDTO> getAllPlans();
    
    List<PlanResponseDTO> getActivePlans();
}
