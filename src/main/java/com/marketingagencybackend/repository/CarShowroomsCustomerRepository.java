package com.marketingagencybackend.repository;

import com.marketingagencybackend.entity.CarShowroomsCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarShowroomsCustomerRepository extends JpaRepository<CarShowroomsCustomer, Long> {

    boolean existsByWhatsappNumber(String whatsappNumber);
}
