package service;

import model.Expense;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// OOP: Interface implementation - XlsxExporter implements Exportable.
public class XlsxExporter implements Exportable {

    private ExpenseService expenseService = new ExpenseService();
    private IncomeService incomeService = new IncomeService();
    private int rowNumber = 0;
    private int mergeCount = 0;
    private StringBuffer mergeCells = new StringBuffer();

    // Writes month by month data to the chosen XLSX file.
    @Override
    public void exportData(int userId, String username, String fileName)
            throws IOException, SQLException {
        ArrayList<Expense> expenses = expenseService.listExpenses(userId);
        HashMap<String, Double> incomes = incomeService.getMonthlyIncomeMap(userId);
        HashMap<String, Double> totals = expenseService.getMonthlySpentMap(userId);
        ArrayList<String> months = getMonths(totals, incomes);
        String sheet = createSheet(username, months, incomes, totals, expenses);
        writeFile(fileName, sheet);
    }

    // Collects all months that have an income or an expense, newest first.
    private ArrayList<String> getMonths(HashMap<String, Double> totals,
                                        HashMap<String, Double> incomes) {
        HashSet<String> monthSet = new HashSet<>();
        for (String month : totals.keySet()) {
            monthSet.add(month);
        }
        for (String month : incomes.keySet()) {
            monthSet.add(month);
        }
        ArrayList<String> months = new ArrayList<>(monthSet);
        Collections.sort(months);
        Collections.reverse(months);
        return months;
    }

    // Builds the complete sheet XML text.
    private String createSheet(String username, ArrayList<String> months,
                               HashMap<String, Double> incomes,
                               HashMap<String, Double> totals,
                               ArrayList<Expense> expenses) {
        rowNumber = 0;
        mergeCount = 0;
        mergeCells = new StringBuffer();
        StringBuffer sheet = new StringBuffer();
        sheet.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
        sheet.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        sheet.append(columnWidths());
        sheet.append("<sheetData>");
        addTitle(sheet, username);
        for (String month : months) {
            addMonth(sheet, month, incomes, totals, expenses);
        }
        addGrandTotal(sheet, incomes, totals);
        sheet.append("</sheetData>");
        sheet.append("<mergeCells count=\"").append(mergeCount).append("\">");
        sheet.append(mergeCells);
        sheet.append("</mergeCells>");
        sheet.append("</worksheet>");
        return sheet.toString();
    }

    // Adds the report title and the date it was created.
    private void addTitle(StringBuffer sheet, String username) {
        rowNumber++;
        sheet.append("<row r=\"").append(rowNumber).append("\" ht=\"26\" customHeight=\"1\">");
        sheet.append(textCell("A", rowNumber, 1, "Expense Report - " + username));
        sheet.append("</row>");
        addMerge(rowNumber);
        rowNumber++;
        sheet.append("<row r=\"").append(rowNumber).append("\">");
        sheet.append(textCell("A", rowNumber, 7,
                "Generated on " + new Date(System.currentTimeMillis())));
        sheet.append("</row>");
        addMerge(rowNumber);
        rowNumber++;
    }

    // Adds one month block with its expenses, income and savings.
    private void addMonth(StringBuffer sheet, String month,
                          HashMap<String, Double> incomes,
                          HashMap<String, Double> totals,
                          ArrayList<Expense> expenses) {
        double income = getValue(incomes, month);
        double spent = getValue(totals, month);
        rowNumber++;
        sheet.append("<row r=\"").append(rowNumber).append("\" ht=\"20\" customHeight=\"1\">");
        sheet.append(textCell("A", rowNumber, 3, monthName(month)));
        sheet.append("</row>");
        addMerge(rowNumber);
        addHeaderRow(sheet);
        for (Expense expense : expenses) {
            if (expense.getDate().startsWith(month)) {
                addExpenseRow(sheet, expense);
            }
        }
        addMoneyRow(sheet, "Month Total", spent);
        addMoneyRow(sheet, "Monthly Income", income);
        addMoneyRow(sheet, "Savings", income - spent);
        rowNumber++;
    }

    // Adds the heading row of the table.
    private void addHeaderRow(StringBuffer sheet) {
        rowNumber++;
        sheet.append("<row r=\"").append(rowNumber).append("\" ht=\"20\" customHeight=\"1\">");
        sheet.append(textCell("A", rowNumber, 2, "Date"));
        sheet.append(textCell("B", rowNumber, 2, "Category"));
        sheet.append(textCell("C", rowNumber, 2, "Description"));
        sheet.append(textCell("D", rowNumber, 2, "Amount"));
        sheet.append("</row>");
    }

    // Adds one expense row with date, category, description and amount.
    private void addExpenseRow(StringBuffer sheet, Expense expense) {
        rowNumber++;
        sheet.append("<row r=\"").append(rowNumber).append("\">");
        sheet.append(textCell("A", rowNumber, 0, expense.getDate()));
        sheet.append(textCell("B", rowNumber, 0, expense.getCategoryName()));
        sheet.append(textCell("C", rowNumber, 0, expense.getDescription()));
        sheet.append(numberCell("D", rowNumber, 4, expense.getAmount()));
        sheet.append("</row>");
    }

    // Adds a label with a money value below a month block.
    private void addMoneyRow(StringBuffer sheet, String label, double amount) {
        rowNumber++;
        sheet.append("<row r=\"").append(rowNumber).append("\">");
        sheet.append(textCell("C", rowNumber, 6, label));
        sheet.append(numberCell("D", rowNumber, 5, amount));
        sheet.append("</row>");
    }

    // Adds the last block with total income, spent and savings.
    private void addGrandTotal(StringBuffer sheet,
                               HashMap<String, Double> incomes,
                               HashMap<String, Double> totals) {
        double income = sumValues(incomes);
        double spent = sumValues(totals);
        rowNumber++;
        sheet.append("<row r=\"").append(rowNumber).append("\" ht=\"20\" customHeight=\"1\">");
        sheet.append(textCell("A", rowNumber, 3, "GRAND TOTAL"));
        sheet.append("</row>");
        addMerge(rowNumber);
        addMoneyRow(sheet, "Total Income", income);
        addMoneyRow(sheet, "Total Spent", spent);
        addMoneyRow(sheet, "Total Savings", income - spent);
    }

    // Adds one cell with text to the sheet.
    private String textCell(String column, int row, int style, String text) {
        return "<c r=\"" + column + row + "\" s=\"" + style
                + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">"
                + Exportable.escapeXml(text) + "</t></is></c>";
    }

    // Adds one cell with a number to the sheet.
    private String numberCell(String column, int row, int style, double value) {
        return "<c r=\"" + column + row + "\" s=\"" + style + "\"><v>"
                + value + "</v></c>";
    }

    // Remembers a row that must be merged from column A to D.
    private void addMerge(int row) {
        mergeCells.append("<mergeCell ref=\"A").append(row)
                .append(":D").append(row).append("\"/>");
        mergeCount++;
    }

    // Returns a month value from a map, or zero if it is missing.
    private double getValue(HashMap<String, Double> map, String month) {
        if (map.containsKey(month)) {
            return map.get(month);
        }
        return 0;
    }

    // Adds all values of a map.
    private double sumValues(HashMap<String, Double> map) {
        double total = 0;
        for (double value : map.values()) {
            total = total + value;
        }
        return total;
    }

    // Turns 2026-11 into NOVEMBER 2026.
    private String monthName(String month) {
        String[] names = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE",
                "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
        int number = Integer.parseInt(month.substring(5, 7));
        return names[number - 1] + " " + month.substring(0, 4);
    }

    // Sets the width of the four columns.
    private String columnWidths() {
        return "<cols><col min=\"1\" max=\"1\" width=\"13\" customWidth=\"1\"/>"
                + "<col min=\"2\" max=\"2\" width=\"20\" customWidth=\"1\"/>"
                + "<col min=\"3\" max=\"3\" width=\"50\" customWidth=\"1\"/>"
                + "<col min=\"4\" max=\"4\" width=\"18\" customWidth=\"1\"/></cols>";
    }

    // Writes all the files that make an XLSX file inside one zip.
    private void writeFile(String fileName, String sheet) throws IOException {
        ZipOutputStream zip = null;
        try {
            zip = new ZipOutputStream(new FileOutputStream(fileName));
            addEntry(zip, "[Content_Types].xml", contentTypes());
            addEntry(zip, "_rels/.rels", rootRels());
            addEntry(zip, "xl/workbook.xml", workbook());
            addEntry(zip, "xl/_rels/workbook.xml.rels", workbookRels());
            addEntry(zip, "xl/styles.xml", styles());
            addEntry(zip, "xl/worksheets/sheet1.xml", sheet);
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
    }

    // Adds one text file inside the XLSX zip.
    private void addEntry(ZipOutputStream zip, String name, String text)
            throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(text.getBytes("UTF-8"));
        zip.closeEntry();
    }

    // Describes the types of files inside the XLSX zip.
    private String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
                + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
                + "</Types>";
    }

    // Points the package to the workbook.
    private String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
                + "</Relationships>";
    }

    // Declares the one worksheet of the workbook.
    private String workbook() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"Expenses\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                + "</workbook>";
    }

    // Points the workbook to the sheet and the styles.
    private String workbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
                + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
                + "</Relationships>";
    }

    // Defines the fonts, colours, money format and row styles.
    private String styles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                + "<numFmts count=\"1\"><numFmt numFmtId=\"164\" formatCode=\"&quot;Rs. &quot;#,##0.00\"/></numFmts>"
                + "<fonts count=\"4\">"
                + "<font><sz val=\"11\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"16\"/><color rgb=\"FF1B5E20\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>"
                + "<font><b/><sz val=\"11\"/><color rgb=\"FF1B5E20\"/><name val=\"Calibri\"/></font>"
                + "</fonts>"
                + "<fills count=\"4\">"
                + "<fill><patternFill patternType=\"none\"/></fill>"
                + "<fill><patternFill patternType=\"gray125\"/></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF2E7D32\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFE8F5E9\"/><bgColor indexed=\"64\"/></patternFill></fill>"
                + "</fills>"
                + "<borders count=\"1\"><border><left/><right/><top/><bottom/><diagonal/></border></borders>"
                + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
                + "<cellXfs count=\"8\">"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"
                + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"2\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\"/></xf>"
                + "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"3\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>"
                + "<xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\"/>"
                + "<xf numFmtId=\"164\" fontId=\"3\" fillId=\"3\" borderId=\"0\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\" applyFill=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"3\" borderId=\"0\" xfId=\"0\" applyFill=\"1\"/>"
                + "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>"
                + "</cellXfs>"
                + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
                + "</styleSheet>";
    }
}
