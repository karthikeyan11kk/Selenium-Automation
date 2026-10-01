package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

 


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.*;

public class Excel_Class {
    private static final Logger log = LoggerFactory.getLogger(Excel_Class.class);


    // Read entire sheet into HashMap<Integer, List<String>>
    public HashMap<Integer, List<String>> xlDataRead(String excelFilePath, String sheetName) throws IOException {
        HashMap<Integer, List<String>> sheetInfo = new HashMap<>();
        FileInputStream fis = new FileInputStream(excelFilePath);
        Workbook workbook = WorkbookFactory.create(fis);
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            log.error("Sheet '" + sheetName + "' not found in Excel file: " + excelFilePath);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                log.info("Available sheet: " + workbook.getSheetName(i));
            }
            throw new RuntimeException("Invalid sheet name: " + sheetName);
        }

        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            List<String> columnInfo = new ArrayList<>();
            if (row != null) {
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    columnInfo.add(cell != null ? cell.toString() : "");
                }
            }
            sheetInfo.put(i, columnInfo);
        }

        workbook.close();
        fis.close();
        return sheetInfo;
    }

    // Get data for a specific test case

public HashMap<String, String> getData(String excelFilePath, String sheetName, String testCaseName) throws IOException {
    HashMap<String, String> dataMap = new HashMap<>();
    try (FileInputStream fis = new FileInputStream(excelFilePath);
         Workbook workbook = WorkbookFactory.create(fis)) {

        Sheet sheet = workbook.getSheet(sheetName);
        Row headerRow = sheet.getRow(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && row.getCell(0).getStringCellValue().equalsIgnoreCase(testCaseName)) {
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell headerCell = headerRow.getCell(j);
                    String key = (headerCell != null) ? headerCell.getStringCellValue().trim() : "Column_" + j;
                    Cell cell = row.getCell(j);
                    String value = "";

                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                value = cell.getStringCellValue().trim();
                                break;
                            case NUMERIC:
                                if (DateUtil.isCellDateFormatted(cell)) {
                                    value = new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
                                } else {
                                    double num = cell.getNumericCellValue();
                                    if (num == Math.floor(num)) {
                                        value = String.valueOf((long) num); // Remove .0 for whole numbers
                                    } else {
                                        value = String.valueOf(num);
                                    }
                                }
                                break;
                            case BOOLEAN:
                                value = String.valueOf(cell.getBooleanCellValue());
                                break;
                            case FORMULA:
                                CellValue cellValue = evaluator.evaluate(cell);
                                switch (cellValue.getCellType()) {
                                    case STRING:
                                        value = cellValue.getStringValue().trim();
                                        break;
                                    case NUMERIC:
                                        if (DateUtil.isCellDateFormatted(cell)) {
                                            value = new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
                                        } else {
                                            double num = cellValue.getNumberValue();
                                            if (num == Math.floor(num)) {
                                                value = String.valueOf((long) num);
                                            } else {
                                                value = String.valueOf(num);
                                            }
                                        }
                                        break;
                                    case BOOLEAN:
                                        value = String.valueOf(cellValue.getBooleanValue());
                                        break;
                                    default:
                                        value = cell.toString().trim();
                                }
                                break;
                            default:
                                value = cell.toString().trim();
                        }
                    }

                    // Handle TODAY() placeholder logic
                    if (value != null && value.toUpperCase().contains("TODAY()")) {
                        int offset = 0;
                        try {
                            if (value.contains("+")) {
                                offset = Integer.parseInt(value.split("\\+")[1].trim());
                            } else if (value.contains("-")) {
                                offset = -Integer.parseInt(value.split("-")[1].trim());
                            }
                        } catch (NumberFormatException e) {
                            log.error("Invalid offset in placeholder: " + value);
                        }
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, offset);
                        value = new SimpleDateFormat("dd/MM/yyyy").format(cal.getTime());
                        log.info("Resolved placeholder for key [" + key + "]: " + value);
                    }

                    dataMap.put(key, value);
                }
                break; // Stop after finding the matching test case
            }
        }
    }
    return dataMap;
}



    // Write data to Excel
    public void writeData(String excelFilePath, String sheetName, int rowNo, String dataToWrite) throws IOException {
        FileInputStream fis = new FileInputStream(excelFilePath);
        Workbook workbook = WorkbookFactory.create(fis);
        Sheet sheet = workbook.getSheet(sheetName);

        Row row = sheet.getRow(rowNo);
        if (row == null) row = sheet.createRow(rowNo);
        int lastCell = row.getLastCellNum() == -1 ? 0 : row.getLastCellNum();
        Cell cell = row.createCell(lastCell);
        cell.setCellValue(dataToWrite);

        fis.close();
        FileOutputStream fos = new FileOutputStream(excelFilePath);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }

    public int getRowCount(String excelFilePath, String sheetName) throws IOException {
        FileInputStream fis = new FileInputStream(excelFilePath);
        Workbook workbook = WorkbookFactory.create(fis);
        Sheet sheet = workbook.getSheet(sheetName);
        int rowCount = sheet.getLastRowNum() + 1;
        workbook.close();
        fis.close();
        return rowCount;
    }

public List<Map<String, String>> getAllData(String excelFilePath, String sheetName) throws IOException {
    List<Map<String, String>> dataList = new ArrayList<>();
    try (FileInputStream fis = new FileInputStream(excelFilePath);
         Workbook workbook = WorkbookFactory.create(fis)) {

        Sheet sheet = workbook.getSheet(sheetName);
        Row headerRow = sheet.getRow(0);
        int colCount = headerRow.getLastCellNum();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Map<String, String> rowData = new HashMap<>();
            for (int j = 0; j < colCount; j++) {
                String key = headerRow.getCell(j).getStringCellValue().trim();
                String value = "";
                Cell cell = row.getCell(j);

                if (cell != null) {
                    switch (cell.getCellType()) {
                        case STRING:
                            value = cell.getStringCellValue().trim();
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                value = new SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
                            } else {
                                double num = cell.getNumericCellValue();
                                if (num == Math.floor(num)) {
                                    value = String.valueOf((long) num); // Remove .0 for whole numbers
                                } else {
                                    value = String.valueOf(num);
                                }
                            }
                            break;
                        case BOOLEAN:
                            value = String.valueOf(cell.getBooleanCellValue());
                            break;
                        default:
                            value = cell.toString().trim();
                    }
                }
                rowData.put(key, value);
            }
            dataList.add(rowData);
        }
    }
    return dataList;
}

}
