package utilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ExcelReader provides utilities for reading test data from .xlsx files
 * using Apache POI. Each row (excluding the header) is returned as a
 * String array, making it easy to feed into TestNG @DataProvider methods.
 */
public class ExcelReader {

    /** Private constructor — utility class, no instantiation. */
    private ExcelReader() {}

    /**
     * Reads all data rows from the specified sheet in an Excel file.
     * The first row is treated as a header and is skipped.
     *
     * @param filePath  path to the .xlsx file
     * @param sheetName name of the sheet to read
     * @return 2-D array of String values (rows × columns), ready for @DataProvider
     * @throws RuntimeException if the file cannot be read
     */
    public static Object[][] getTestData(String filePath, String sheetName) {
        List<Object[]> data = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException(
                        "Sheet '" + sheetName + "' not found in " + filePath);
            }

            int lastRow = sheet.getLastRowNum();
            // Row 0 is the header — start from row 1
            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                int lastCol = row.getLastCellNum();
                String[] rowData = new String[lastCol];
                for (int j = 0; j < lastCol; j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowData[j] = getCellValueAsString(cell);
                }
                data.add(rowData);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file: " + filePath, e);
        }

        return data.toArray(new Object[0][]);
    }

    /**
     * Converts a Cell value to its String representation regardless of cell type.
     *
     * @param cell the POI Cell to convert
     * @return String value of the cell
     */
    private static String getCellValueAsString(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }
}
