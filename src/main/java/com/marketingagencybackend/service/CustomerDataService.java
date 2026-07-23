package com.marketingagencybackend.service;

import com.marketingagencybackend.dto.ExcelImportResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface CustomerDataService {
    ExcelImportResponseDTO importFromExcel(MultipartFile file, Long clientId, com.marketingagencybackend.enums.BusinessCategory businessCategory);
    java.util.List<com.marketingagencybackend.dto.CustomerDataResponseDTO> getCustomerDataByClientId(Long clientId);
    java.util.List<com.marketingagencybackend.dto.ImportLogResponseDTO> getImportLogsByClientId(Long clientId);
}
