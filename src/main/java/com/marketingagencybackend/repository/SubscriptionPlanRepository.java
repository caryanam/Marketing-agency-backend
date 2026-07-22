package com.marketingagencybackend.repository;

import com.marketingagencybackend.entity.SubscriptionPlan;
import com.marketingagencybackend.enums.PlanCode;
import com.marketingagencybackend.enums.PlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    
    Optional<SubscriptionPlan> findByPlanCode(PlanCode planCode);
    
    List<SubscriptionPlan> findByIsActiveTrue();
    
    List<SubscriptionPlan> findByPlanType(PlanType planType);
}
