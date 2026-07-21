package com.marketingagencybackend.repository;

import com.marketingagencybackend.entity.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerDataRepository extends JpaRepository<CustomerData, Long> {
    boolean existsByWhatsappNumber(String whatsappNumber);
}
