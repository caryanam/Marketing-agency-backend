package com.marketingagencybackend.service.serviceImpl;

import com.marketingagencybackend.dto.CustomerDataResponseDTO;
import com.marketingagencybackend.dto.ExcelImportResponseDTO;
import com.marketingagencybackend.entity.CustomerData;
import com.marketingagencybackend.repository.CustomerDataRepository;
import com.marketingagencybackend.service.CustomerDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomerDataServiceImpl implements CustomerDataService {

    private static final int NAME_COLUMN_INDEX = 0;
    private static final int WHATSAPP_COLUMN_INDEX = 1;
    private static final int HEADER_ROW_INDEX = 0;

    private final CustomerDataRepository customerDataRepository;

    @Override
    public ExcelImportResponseDTO importFromExcel(MultipartFile file) {

        validateFile(file);

        int totalRowsRead = 0;
        int skippedEmptyRows = 0;
        int skippedInvalidRows = 0;
        int skippedDuplicateRows = 0;

        List<CustomerData> customersToSave = new ArrayList<>();
        Set<String> seenWhatsappNumbers = new HashSet<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            int lastRowNum = sheet.getLastRowNum();

            for (int rowIndex = HEADER_ROW_INDEX + 1; rowIndex <= lastRowNum; rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (isRowEmpty(row)) {
                    skippedEmptyRows++;
                    continue;
                }

                totalRowsRead++;

                String customerName = getCellValueAsString(row.getCell(NAME_COLUMN_INDEX));
                String whatsappNumber = getCellValueAsString(row.getCell(WHATSAPP_COLUMN_INDEX));

                if (customerName.isBlank() || whatsappNumber.isBlank()) {
                    log.warn("Skipping invalid row {} - missing customer name or whatsapp number", rowIndex + 1);
                    skippedInvalidRows++;
                    continue;
                }

                // Check for duplicate whatsapp number in database or within the same Excel sheet batch
                if (customerDataRepository.existsByWhatsappNumber(whatsappNumber) || !seenWhatsappNumbers.add(whatsappNumber)) {
                    log.info("Skipping duplicate row {} with WhatsApp number {}", rowIndex + 1, whatsappNumber);
                    skippedDuplicateRows++;
                    continue;
                }

                customersToSave.add(
                        CustomerData.builder()
                                .customerName(customerName)
                                .whatsappNumber(whatsappNumber)
                                .build()
                );
            }

        } catch (IOException e) {
            log.error("Failed to read excel file", e);
            throw new IllegalArgumentException("Unable to read the uploaded excel file. Please upload a valid .xlsx or .xls file.");
        }

        List<CustomerData> savedCustomers = customerDataRepository.saveAll(customersToSave);

        log.info("Excel import completed. Read: {}, Imported: {}, Skipped empty: {}, Skipped invalid: {}, Skipped duplicate: {}",
                totalRowsRead, savedCustomers.size(), skippedEmptyRows, skippedInvalidRows, skippedDuplicateRows);

        List<CustomerDataResponseDTO> responseDTOs = savedCustomers.stream()
                .map(CustomerDataResponseDTO::from)
                .toList();

        return new ExcelImportResponseDTO(
                totalRowsRead,
                savedCustomers.size(),
                skippedEmptyRows,
                skippedInvalidRows,
                skippedDuplicateRows,
                responseDTOs
        );
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a non-empty excel file.");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null ||
                !(fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"))) {
            throw new IllegalArgumentException("Invalid file type. Only .xlsx or .xls files are supported.");
        }
    }

    private boolean isRowEmpty(Row row) {

        if (row == null) {
            return true;
        }

        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK &&
                    !getCellValueAsString(cell).isBlank()) {
                return false;
            }
        }

        return true;
    }

    private String getCellValueAsString(Cell cell) {

        if (cell == null) {
            return "";
        }

        CellType cellType = cell.getCellType();

        if (cellType == CellType.FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }

        return switch (cellType) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) {
                    yield String.valueOf((long) numericValue);
                }
                yield String.valueOf(numericValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
