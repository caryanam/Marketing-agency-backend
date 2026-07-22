package com.marketingagencybackend.repository;

import com.marketingagencybackend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email);
    
    List<Notification> findByRecipientEmailAndIsReadFalseOrderByCreatedAtDesc(String email);
    
    long countByRecipientEmailAndIsReadFalse(String email);
}
