package com.marketingagencybackend.service.serviceImpl;

import com.marketingagencybackend.dto.CustomerDataResponseDTO;
import com.marketingagencybackend.dto.ExcelImportResponseDTO;
import com.marketingagencybackend.entity.Client;
import com.marketingagencybackend.entity.CustomerData;
import com.marketingagencybackend.repository.ClientRepository;
import com.marketingagencybackend.service.CustomerDataService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
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


    private final JdbcTemplate jdbcTemplate;
    private final ClientRepository clientRepository;

    @Override
    public ExcelImportResponseDTO importFromExcel(MultipartFile file, Long clientId, com.marketingagencybackend.enums.BusinessCategory businessCategory) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found with ID: " + clientId));
        
        if (client.getCategory() != businessCategory) {
            throw new IllegalArgumentException("Provided business category does not match the client's registered category: " + client.getCategory());
        }

        validateFile(file);

        // Dynamically create table for this specific client
        String tableName = "customer_data_" + clientId;
        createTableIfNotExists(tableName);

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

                // Check for duplicate whatsapp number in dynamic table or within the same Excel sheet batch
                if (existsByWhatsappNumberDynamic(tableName, whatsappNumber) || !seenWhatsappNumbers.add(whatsappNumber)) {
                    log.info("Skipping duplicate row {} with WhatsApp number {}", rowIndex + 1, whatsappNumber);
                    skippedDuplicateRows++;
                    continue;
                }

                customersToSave.add(
                        CustomerData.builder()
                                .customerName(customerName)
                                .whatsappNumber(whatsappNumber)
                                .clientId(clientId)
                                .businessCategory(businessCategory)
                                .build()
                );
            }

        } catch (IOException e) {
            log.error("Failed to read excel file", e);
            throw new IllegalArgumentException("Unable to read the uploaded excel file. Please upload a valid .xlsx or .xls file.");
        }

        // Batch Insert into dynamic table
        if (!customersToSave.isEmpty()) {
            String insertSql = "INSERT INTO " + tableName + " (customer_name, whatsapp_number, business_category, client_id) VALUES (?, ?, ?, ?)";
            jdbcTemplate.batchUpdate(insertSql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    CustomerData data = customersToSave.get(i);
                    ps.setString(1, data.getCustomerName());
                    ps.setString(2, data.getWhatsappNumber());
                    ps.setString(3, data.getBusinessCategory() != null ? data.getBusinessCategory().name() : null);
                    ps.setLong(4, data.getClientId());
                }

                @Override
                public int getBatchSize() {
                    return customersToSave.size();
                }
            });
        }

        log.info("Excel import completed for table {}. Read: {}, Imported: {}, Skipped empty: {}, Skipped invalid: {}, Skipped duplicate: {}",
                tableName, totalRowsRead, customersToSave.size(), skippedEmptyRows, skippedInvalidRows, skippedDuplicateRows);

        List<CustomerDataResponseDTO> responseDTOs = new ArrayList<>();
        if (!customersToSave.isEmpty()) {
            NamedParameterJdbcTemplate namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
            List<String> whatsappNumbers = customersToSave.stream()
                    .map(CustomerData::getWhatsappNumber)
                    .toList();

            String querySql = "SELECT * FROM " + tableName + " WHERE whatsapp_number IN (:numbers)";
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("numbers", whatsappNumbers);
            
            responseDTOs = namedJdbcTemplate.query(querySql, parameters, getCustomerDataRowMapper());
        }

        return new ExcelImportResponseDTO(
                totalRowsRead,
                customersToSave.size(),
                skippedEmptyRows,
                skippedInvalidRows,
                skippedDuplicateRows,
                responseDTOs
        );
    }

    @Override
    public List<CustomerDataResponseDTO> getCustomerDataByClientId(Long clientId) {
        String tableName = "customer_data_" + clientId;
        // Check if table exists first to avoid SQL errors
        if (!isTableExists(tableName)) {
            return new ArrayList<>();
        }

        String sql = "SELECT * FROM " + tableName;
        return jdbcTemplate.query(sql, getCustomerDataRowMapper());
    }

    private RowMapper<CustomerDataResponseDTO> getCustomerDataRowMapper() {
        return (rs, rowNum) -> {
            LocalDateTime createdAt = rs.getTimestamp("created_at") != null ? 
                                      rs.getTimestamp("created_at").toLocalDateTime() : null;
            String bcStr = rs.getString("business_category");
            com.marketingagencybackend.enums.BusinessCategory bc = bcStr != null ? com.marketingagencybackend.enums.BusinessCategory.valueOf(bcStr) : null;
            return new CustomerDataResponseDTO(
                    rs.getLong("id"),
                    rs.getString("customer_name"),
                    rs.getString("whatsapp_number"),
                    rs.getLong("client_id"),
                    bc,
                    createdAt
            );
        };
    }

    private void createTableIfNotExists(String tableName) {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "customer_name VARCHAR(255) NOT NULL, " +
                "whatsapp_number VARCHAR(20) NOT NULL UNIQUE, " +
                "business_category VARCHAR(255), " +
                "client_id BIGINT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "INDEX idx_business_category (business_category)" +
                ")";
        jdbcTemplate.execute(sql);
        log.info("Ensured dynamic table {} exists.", tableName);
    }

    private boolean existsByWhatsappNumberDynamic(String tableName, String whatsappNumber) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE whatsapp_number = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, whatsappNumber);
        return count != null && count > 0;
    }
    
    private boolean isTableExists(String tableName) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName);
        return count != null && count > 0;
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
