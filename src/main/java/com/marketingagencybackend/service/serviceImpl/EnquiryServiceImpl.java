package com.marketingagencybackend.service.serviceImpl;

import com.marketingagencybackend.dto.EnquiryRequestDTO;
import com.marketingagencybackend.dto.EnquiryResponseDTO;
import com.marketingagencybackend.entity.Enquiry;
import com.marketingagencybackend.exception.ResourceNotFoundException;
import com.marketingagencybackend.repository.EnquiryRepository;
import com.marketingagencybackend.service.EnquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnquiryServiceImpl implements EnquiryService {

    private final EnquiryRepository enquiryRepository;

    @Override
    public EnquiryResponseDTO createEnquiry(EnquiryRequestDTO requestDTO) {
        log.info("Creating new enquiry for email: {}", requestDTO.email());
        
        Enquiry enquiry = Enquiry.builder()
                .name(requestDTO.name())
                .phoneNumber(requestDTO.phoneNumber())
                .email(requestDTO.email())
                .goals(requestDTO.goals())
                .build();
                
        Enquiry savedEnquiry = enquiryRepository.save(enquiry);
        
        return EnquiryResponseDTO.builder()
                .id(savedEnquiry.getId())
                .name(savedEnquiry.getName())
                .phoneNumber(savedEnquiry.getPhoneNumber())
                .email(savedEnquiry.getEmail())
                .goals(savedEnquiry.getGoals())
                .createdAt(savedEnquiry.getCreatedAt())
                .build();
    }

    @Override
    public List<EnquiryResponseDTO> getAllEnquiries() {
        log.info("Fetching all enquiries");
        return enquiryRepository.findAll().stream()
                .map(enquiry -> EnquiryResponseDTO.builder()
                        .id(enquiry.getId())
                        .name(enquiry.getName())
                        .phoneNumber(enquiry.getPhoneNumber())
                        .email(enquiry.getEmail())
                        .goals(enquiry.getGoals())
                        .createdAt(enquiry.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public EnquiryResponseDTO getEnquiryById(Long id) {
        log.info("Fetching enquiry by id: {}", id);
        Enquiry enquiry = enquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enquiry not found with id: " + id));
        return EnquiryResponseDTO.builder()
                .id(enquiry.getId())
                .name(enquiry.getName())
                .phoneNumber(enquiry.getPhoneNumber())
                .email(enquiry.getEmail())
                .goals(enquiry.getGoals())
                .createdAt(enquiry.getCreatedAt())
                .build();
    }


}
