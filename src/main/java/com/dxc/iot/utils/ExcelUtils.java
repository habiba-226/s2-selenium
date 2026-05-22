package com.dxc.iot.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtils {

  public static Object[][] getSheetData(String filePath, String sheetName) {
    List<Object[]> data = new ArrayList<>();
    try (FileInputStream fis = new FileInputStream(filePath);
        Workbook wb = new XSSFWorkbook(fis)) {

      Sheet sheet = wb.getSheet(sheetName);
      if (sheet == null) {
        throw new RuntimeException("Sheet not found: " + sheetName);
      }

      int lastRow = sheet.getLastRowNum();
      int cols = sheet.getRow(0).getLastCellNum();

      for (int i = 1; i <= lastRow; i++) {
        Row row = sheet.getRow(i);
        if (row == null)
          continue;
        Object[] rowData = new Object[cols];
        for (int j = 0; j < cols; j++) {
          Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
          rowData[j] = getCellValue(cell);
        }
        data.add(rowData);
      }
    } catch (Exception e) {
      throw new RuntimeException("Excel read failed: " + filePath, e);
    }
    return data.toArray(new Object[0][]);
  }

  private static String getCellValue(Cell cell) {
    if (cell == null)
      return "";
    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue().trim();
      case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      case BLANK -> "";
      default -> cell.toString();
    };
  }
}