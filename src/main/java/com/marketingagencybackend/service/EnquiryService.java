package com.marketingagencybackend.service;

import com.marketingagencybackend.dto.EnquiryRequestDTO;
import com.marketingagencybackend.dto.EnquiryResponseDTO;

import java.util.List;

public interface EnquiryService {
    
    EnquiryResponseDTO createEnquiry(EnquiryRequestDTO requestDTO);
    
    List<EnquiryResponseDTO> getAllEnquiries();
    
    EnquiryResponseDTO getEnquiryById(Long id);

}
