package com.marketingagencybackend.service.serviceImpl;

import com.marketingagencybackend.dto.CarShowroomsCustomerResponseDTO;
import com.marketingagencybackend.dto.ExcelImportResponseDTO;
import com.marketingagencybackend.entity.CarShowroomsCustomer;
import com.marketingagencybackend.repository.CarShowroomsCustomerRepository;
import com.marketingagencybackend.service.CarShowroomsCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CarShowroomsCustomerServiceImpl implements CarShowroomsCustomerService {

    private static final int NAME_COLUMN_INDEX = 0;
    private static final int WHATSAPP_COLUMN_INDEX = 1;
    private static final int HEADER_ROW_INDEX = 0;

    private final CarShowroomsCustomerRepository carShowroomsCustomerRepository;

    @Override
    public ExcelImportResponseDTO importFromExcel(MultipartFile file) {

        validateFile(file);

        int totalRowsRead = 0;
        int skippedEmptyRows = 0;
        int skippedInvalidRows = 0;

        List<CarShowroomsCustomer> customersToSave = new ArrayList<>();

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

                String name = getCellValueAsString(row.getCell(NAME_COLUMN_INDEX));
                String whatsappNumber = getCellValueAsString(row.getCell(WHATSAPP_COLUMN_INDEX));

                if (name.isBlank() || whatsappNumber.isBlank()) {
                    log.warn("Skipping invalid row {} - missing name or whatsapp number", rowIndex + 1);
                    skippedInvalidRows++;
                    continue;
                }

                customersToSave.add(
                        CarShowroomsCustomer.builder()
                                .name(name)
                                .whatsappNumber(whatsappNumber)
                                .build()
                );
            }

        } catch (IOException e) {
            log.error("Failed to read excel file", e);
            throw new IllegalArgumentException("Unable to read the uploaded excel file. Please upload a valid .xlsx or .xls file.");
        }

        List<CarShowroomsCustomer> savedCustomers = carShowroomsCustomerRepository.saveAll(customersToSave);

        log.info("Excel import completed. Read: {}, Imported: {}, Skipped empty: {}, Skipped invalid: {}",
                totalRowsRead, savedCustomers.size(), skippedEmptyRows, skippedInvalidRows);

        List<CarShowroomsCustomerResponseDTO> responseDTOs = savedCustomers.stream()
                .map(CarShowroomsCustomerResponseDTO::from)
                .toList();

        return new ExcelImportResponseDTO(
                totalRowsRead,
                savedCustomers.size(),
                skippedEmptyRows,
                skippedInvalidRows,
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
