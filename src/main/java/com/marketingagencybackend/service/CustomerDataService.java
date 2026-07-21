package com.marketingagencybackend.service;

import com.marketingagencybackend.dto.ExcelImportResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface CustomerDataService {
    ExcelImportResponseDTO importFromExcel(MultipartFile file);
}
