package com.marketingagencybackend.service;

import com.marketingagencybackend.dto.ExcelImportResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface CarShowroomsCustomerService {

    ExcelImportResponseDTO importFromExcel(MultipartFile file);
}
