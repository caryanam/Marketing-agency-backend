package com.marketingagencybackend.repository;

import com.marketingagencybackend.entity.CustomerData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerDataRepository extends JpaRepository<CustomerData, Long> {
    List<CustomerData> findByClientId(Long clientId);
    Optional<CustomerData> findByClientIdAndWhatsappNumber(Long clientId, String whatsappNumber);
}
