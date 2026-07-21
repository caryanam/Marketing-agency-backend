package com.marketingagencybackend.dto;

import java.util.List;

public record ExcelImportResponseDTO(

        int totalRowsRead,
        int totalImported,
        int totalSkippedEmptyRows,
        int totalSkippedInvalidRows,
        List<CarShowroomsCustomerResponseDTO> importedCustomers

) {
}
