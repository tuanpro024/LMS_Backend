package com.lms.learningpath.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Container for parsed Learning Path Excel data.
 * Memory-safe: stores content sheets as byte[] instead of Sheet objects
 * to avoid lifecycle issues after Workbook closure.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearningPathExcelData {
    
    /**
     * Parsed rows from the "Structure" sheet
     */
    private List<LearningPathStructureRow> structureRows;
    
    /**
     * Content sheets extracted as byte arrays.
     * Key: sheet name (matches contentSheetName in structure rows)
     * Value: byte[] of workbook containing a single sheet
     * 
     * Using byte[] ensures data is preserved after original Workbook is closed.
     */
    private Map<String, byte[]> contentSheetBytes = new LinkedHashMap<>();
}
