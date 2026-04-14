package utilities;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * TestDataGenerator creates the testdata.xlsx file with sample invalid-login
 * scenarios used by the {@code verifyInvalidLogin} DataProvider in LoginTest.
 *
 * <p>Run this class once as a standalone main() to (re)generate the file.
 * It is NOT part of the test suite execution.</p>
 */
public class TestDataGenerator {

    private static final String OUTPUT_PATH = "src/test/resources/testdata.xlsx";

    /** Private constructor — utility class, no instantiation. */
    private TestDataGenerator() {}

    /**
     * Entry point for generating the Excel test data file.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        generateTestData();
    }

    /**
     * Creates testdata.xlsx with an "InvalidLogin" sheet containing
     * sample invalid credential rows and expected error messages.
     */
    public static void generateTestData() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("InvalidLogin");

            // ── Header row ──────────────────────────────────────────────────
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row header = sheet.createRow(0);
            String[] columns = {"username", "password", "expectedError"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // ── Data rows ───────────────────────────────────────────────────
            // Only wrong-credential rows (non-empty username AND password).
            // Empty-field scenarios are covered by verifyEmptyCredentialsError().
            Object[][] testData = {
                {"Admin",        "wrongpassword",   "Invalid credentials"},
                {"wronguser",    "admin123",         "Invalid credentials"},
                {"wronguser",    "wrongpassword",    "Invalid credentials"},
            };

            for (int i = 0; i < testData.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < testData[i].length; j++) {
                    row.createCell(j).setCellValue((String) testData[i][j]);
                }
            }

            // Auto-size columns after data is written
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(OUTPUT_PATH)) {
                workbook.write(fos);
            }

            System.out.println("testdata.xlsx generated at: " + OUTPUT_PATH);

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate testdata.xlsx", e);
        }
    }
}
