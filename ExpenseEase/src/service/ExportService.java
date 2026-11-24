package service;

import model.Expense;
import model.IncomeEntry;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Handles CSV, XML and Excel exports.
public class ExportService implements Exportable {

    private ExpenseService expenseService = new ExpenseService();
    private IncomeService incomeService = new IncomeService();

    // OOP: Interface implementation.
    public void exportData(int userId, String username, String fileName)
            throws IOException, SQLException {
        exportXlsx(userId, username, fileName, new ArrayList<String>());
    }

    // Export to CSV.
    public void exportCsv(int userId, String username, String fileName)
            throws IOException, SQLException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write("ExpenseEase CSV Export - " + username);
        writer.newLine();
        writer.write("EXPENSES");
        writer.newLine();
        writer.write("Date,Category,Amount,Description");
        writer.newLine();
        for (Expense expense : expenseService.listExpenses(userId)) {
            writer.write(csv(expense.getDate()) + "," + csv(expense.getCategoryName())
                    + "," + expense.getAmount() + "," + csv(expense.getDescription()));
            writer.newLine();
        }
        writer.write("MONTHLY INCOME");
        writer.newLine();
        writer.write("Month,Amount");
        writer.newLine();
        HashMap<String, Double> incomes = incomeService.getMonthlyIncomeMap(userId);
        List<String> months = new ArrayList<>(incomes.keySet());
        Collections.sort(months);
        for (String month : months) {
            writer.write(csv(month) + "," + incomes.get(month));
            writer.newLine();
        }
        writer.write("EXTRA INCOME");
        writer.newLine();
        writer.write("Date,Type,Amount,Description");
        writer.newLine();
        for (IncomeEntry entry : incomeService.listExtraIncome(userId)) {
            writer.write(csv(entry.getDate()) + "," + csv(entry.getIncomeType())
                    + "," + entry.getAmount() + "," + csv(entry.getDescription()));
            writer.newLine();
        }
        writer.close();
    }

    // Export to XML.
    public void exportXml(int userId, String username, String fileName)
            throws IOException, SQLException {
        ArrayList<Expense> expenses = expenseService.listExpenses(userId);
        HashMap<String, Double> incomes = incomeService.getMonthlyIncomeMap(userId);
        HashMap<String, Double> extraMap = incomeService.getMonthlyExtraIncomeMap(userId);
        HashMap<String, Double> totals = expenseService.getMonthlySpentMap(userId);
        Set<String> monthSet = new HashSet<>();
        monthSet.addAll(totals.keySet());
        monthSet.addAll(incomes.keySet());
        monthSet.addAll(extraMap.keySet());
        ArrayList<String> months = new ArrayList<>(monthSet);
        Collections.sort(months);
        Collections.reverse(months);

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.newLine();
        writer.write("<expenseReport user=\"" + escape(username) + "\">");
        writer.newLine();
        for (String month : months) {
            writer.write("    <month name=\"" + escape(month) + "\">");
            writer.newLine();
            writer.write("        <income>" + getValue(incomes, month) + "</income>");
            writer.newLine();
            writer.write("        <extraIncome>" + getValue(extraMap, month) + "</extraIncome>");
            writer.newLine();
            for (Expense expense : expenses) {
                if (expense.getDate().startsWith(month)) {
                    writer.write("        <expense>");
                    writer.newLine();
                    writer.write("            <category>" + escape(expense.getCategoryName()) + "</category>");
                    writer.newLine();
                    writer.write("            <amount>" + expense.getAmount() + "</amount>");
                    writer.newLine();
                    writer.write("            <date>" + escape(expense.getDate()) + "</date>");
                    writer.newLine();
                    writer.write("            <description>" + escape(expense.getDescription()) + "</description>");
                    writer.newLine();
                    writer.write("        </expense>");
                    writer.newLine();
                }
            }
            writer.write("        <totalSpent>" + getValue(totals, month) + "</totalSpent>");
            writer.newLine();
            writer.write("        <savings>" + (getValue(incomes, month) + getValue(extraMap, month)
                    - getValue(totals, month)) + "</savings>");
            writer.newLine();
            writer.write("    </month>");
            writer.newLine();
        }
        writer.write("</expenseReport>");
        writer.newLine();
        writer.close();
    }

    // Export to Excel with Apache POI.
    public void exportXlsx(int userId, String username, String fileName,
                           List<String> charts) throws IOException, SQLException {
        ArrayList<Expense> allExpenses = expenseService.listExpenses(userId);
        HashMap<String, Double> incomes = incomeService.getMonthlyIncomeMap(userId);
        HashMap<String, Double> extraMap = incomeService.getMonthlyExtraIncomeMap(userId);
        HashMap<String, Double> totals = expenseService.getMonthlySpentMap(userId);
        HashMap<String, Double> budgets = new HashMap<>();
        // Collect budget values for available months.
        for (String month : totals.keySet()) {
            budgets.put(month, expenseService.getBudget(userId, month));
        }
        for (String month : incomes.keySet()) {
            if (!budgets.containsKey(month)) {
                budgets.put(month, expenseService.getBudget(userId, month));
            }
        }

        Set<String> monthSet = new HashSet<>();
        monthSet.addAll(totals.keySet());
        monthSet.addAll(incomes.keySet());
        monthSet.addAll(extraMap.keySet());
        ArrayList<String> allMonths = new ArrayList<>(monthSet);
        Collections.sort(allMonths);
        Collections.reverse(allMonths);
        ArrayList<String> selectedMonths = new ArrayList<>();
        for (int i = 0; i < allMonths.size() && i < 6; i++) {
            selectedMonths.add(allMonths.get(i));
        }
        Set<String> selectedSet = new HashSet<>(selectedMonths);

        XSSFWorkbook workbook = new XSSFWorkbook();

        // Sheet 1: Income
        XSSFSheet incomeSheet = workbook.createSheet("Income");
        int r = 0;
        Row titleRow = incomeSheet.createRow(r++);
        titleRow.createCell(0).setCellValue("Monthly Income");
        Row header = incomeSheet.createRow(r++);
        header.createCell(0).setCellValue("Month");
        header.createCell(1).setCellValue("Amount");
        for (String month : selectedMonths) {
            Row row = incomeSheet.createRow(r++);
            row.createCell(0).setCellValue(month);
            row.createCell(1).setCellValue(getValue(incomes, month));
        }
        r++;
        Row extraTitle = incomeSheet.createRow(r++);
        extraTitle.createCell(0).setCellValue("Extra Income");
        Row extraHeader = incomeSheet.createRow(r++);
        extraHeader.createCell(0).setCellValue("Date");
        extraHeader.createCell(1).setCellValue("Type");
        extraHeader.createCell(2).setCellValue("Description");
        extraHeader.createCell(3).setCellValue("Amount");
        for (IncomeEntry entry : incomeService.listExtraIncome(userId)) {
            if (selectedSet.contains(entry.getDate().substring(0, 7))) {
                Row row = incomeSheet.createRow(r++);
                row.createCell(0).setCellValue(entry.getDate());
                row.createCell(1).setCellValue(entry.getIncomeType());
                row.createCell(2).setCellValue(entry.getDescription());
                row.createCell(3).setCellValue(entry.getAmount());
            }
        }

        // Sheet 2: Expenses
        XSSFSheet expenseSheet = workbook.createSheet("Expenses");
        r = 0;
        Row eHeader = expenseSheet.createRow(r++);
        eHeader.createCell(0).setCellValue("Date");
        eHeader.createCell(1).setCellValue("Category");
        eHeader.createCell(2).setCellValue("Description");
        eHeader.createCell(3).setCellValue("Amount");
        for (Expense expense : allExpenses) {
            if (selectedSet.contains(expense.getDate().substring(0, 7))) {
                Row row = expenseSheet.createRow(r++);
                row.createCell(0).setCellValue(expense.getDate());
                row.createCell(1).setCellValue(expense.getCategoryName());
                row.createCell(2).setCellValue(expense.getDescription());
                row.createCell(3).setCellValue(expense.getAmount());
            }
        }

        // Sheet 3: Monthly Summary
        XSSFSheet summarySheet = workbook.createSheet("Monthly Summary");
        r = 0;
        Row sHeader = summarySheet.createRow(r++);
        sHeader.createCell(0).setCellValue("Month");
        sHeader.createCell(1).setCellValue("Total Income");
        sHeader.createCell(2).setCellValue("Extra Income");
        sHeader.createCell(3).setCellValue("Total Expenses");
        sHeader.createCell(4).setCellValue("Savings");
        sHeader.createCell(5).setCellValue("Budget");
        sHeader.createCell(6).setCellValue("Budget Usage %");
        for (String month : selectedMonths) {
            double inc = getValue(incomes, month);
            double ext = getValue(extraMap, month);
            double spent = getValue(totals, month);
            double budget = getValue(budgets, month);
            Row row = summarySheet.createRow(r++);
            row.createCell(0).setCellValue(month);
            row.createCell(1).setCellValue(inc);
            row.createCell(2).setCellValue(ext);
            row.createCell(3).setCellValue(spent);
            row.createCell(4).setCellValue(inc + ext - spent);
            row.createCell(5).setCellValue(budget);
            if (budget > 0) {
                row.createCell(6).setCellValue((spent / budget) * 100.0);
            } else {
                row.createCell(6).setCellValue(0);
            }
        }

        // Sheet 4: Category Summary
        XSSFSheet categorySheet = workbook.createSheet("Category Summary");
        r = 0;
        Row cHeader = categorySheet.createRow(r++);
        cHeader.createCell(0).setCellValue("Category");
        cHeader.createCell(1).setCellValue("Total Spent");
        cHeader.createCell(2).setCellValue("Percentage");
        HashMap<String, Double> categoryTotals = new HashMap<>();
        double grandTotal = 0;
        for (Expense expense : allExpenses) {
            if (selectedSet.contains(expense.getDate().substring(0, 7))) {
                double old = categoryTotals.containsKey(expense.getCategoryName())
                        ? categoryTotals.get(expense.getCategoryName()) : 0;
                categoryTotals.put(expense.getCategoryName(), old + expense.getAmount());
                grandTotal = grandTotal + expense.getAmount();
            }
        }
        List<String> catNames = new ArrayList<>(categoryTotals.keySet());
        Collections.sort(catNames);
        for (String cat : catNames) {
            double val = categoryTotals.get(cat);
            Row row = categorySheet.createRow(r++);
            row.createCell(0).setCellValue(cat);
            row.createCell(1).setCellValue(val);
            if (grandTotal > 0) {
                row.createCell(2).setCellValue((val / grandTotal) * 100.0);
            } else {
                row.createCell(2).setCellValue(0);
            }
        }

        // Sheet 5: Statistics
        XSSFSheet statsSheet = workbook.createSheet("Statistics");
        r = 0;
        statsSheet.createRow(r++).createCell(0).setCellValue("Statistic");
        // We'll put label in col 0 and value in col 1 with header.
        // Rebuild header properly:
        // (The above created an extra row; adjust by using current r, no issue.)
        Row stHeader = statsSheet.createRow(r++);
        stHeader.createCell(0).setCellValue("Name");
        stHeader.createCell(1).setCellValue("Value");
        double totalIncome = incomeService.getTotalIncome(userId) + incomeService.getTotalExtraIncome(userId);
        double totalSpent = expenseService.getTotalSpent(userId);
        stHeader = null;
        addStat(statsSheet, r++, "Total Income", totalIncome);
        addStat(statsSheet, r++, "Total Expenses", totalSpent);
        addStat(statsSheet, r++, "Total Savings", totalIncome - totalSpent);
        addStat(statsSheet, r++, "Number of Expenses", allExpenses.size());

        // Charts
        if (charts != null && !charts.isEmpty()) {
            XSSFSheet chartSheet = workbook.createSheet("Charts");
            int chartRow = 0;
            // Prepare monthly arrays (ascending for charts)
            ArrayList<String> chartMonths = new ArrayList<>(selectedMonths);
            Collections.sort(chartMonths);
            String[] monthLabels = chartMonths.toArray(new String[0]);
            double[] spentValues = new double[monthLabels.length];
            double[] incomeValues = new double[monthLabels.length];
            double[] extraValues = new double[monthLabels.length];
            double[] savingsValues = new double[monthLabels.length];
            double[] budgetValues = new double[monthLabels.length];
            for (int i = 0; i < monthLabels.length; i++) {
                spentValues[i] = getValue(totals, monthLabels[i]);
                incomeValues[i] = getValue(incomes, monthLabels[i]);
                extraValues[i] = getValue(extraMap, monthLabels[i]);
                savingsValues[i] = incomeValues[i] + extraValues[i] - spentValues[i];
                budgetValues[i] = getValue(budgets, monthLabels[i]);
            }

            for (String chart : charts) {
                if ("Monthly Expenditure".equals(chart)) {
                    addBarChart(chartSheet, chartRow, "Monthly Expenditure",
                            monthLabels, spentValues, "Spent");
                    chartRow = chartRow + 22;
                } else if ("Monthly Income vs Expenses".equals(chart)) {
                    addTwoSeriesChart(chartSheet, chartRow, "Income vs Expenses",
                            monthLabels, incomeValues, extraValues, spentValues);
                    chartRow = chartRow + 22;
                } else if ("Category Spending".equals(chart)) {
                    List<String> cats = new ArrayList<>(categoryTotals.keySet());
                    Collections.sort(cats);
                    String[] catLabels = cats.toArray(new String[0]);
                    double[] catValues = new double[catLabels.length];
                    for (int i = 0; i < catLabels.length; i++) {
                        catValues[i] = categoryTotals.get(catLabels[i]);
                    }
                    addBarChart(chartSheet, chartRow, "Category Spending",
                            catLabels, catValues, "Spent");
                    chartRow = chartRow + 22;
                } else if ("Category Pie".equals(chart)) {
                    List<String> cats = new ArrayList<>(categoryTotals.keySet());
                    Collections.sort(cats);
                    String[] catLabels = cats.toArray(new String[0]);
                    double[] catValues = new double[catLabels.length];
                    for (int i = 0; i < catLabels.length; i++) {
                        catValues[i] = categoryTotals.get(catLabels[i]);
                    }
                    addPieChart(chartSheet, chartRow, "Category Share", catLabels, catValues);
                    chartRow = chartRow + 22;
                } else if ("Monthly Savings".equals(chart)) {
                    addBarChart(chartSheet, chartRow, "Monthly Savings",
                            monthLabels, savingsValues, "Savings");
                    chartRow = chartRow + 22;
                } else if ("Budget vs Actual".equals(chart)) {
                    addTwoSeriesChart(chartSheet, chartRow, "Budget vs Actual",
                            monthLabels, budgetValues, new double[monthLabels.length], spentValues);
                    chartRow = chartRow + 22;
                }
            }
        }

        File file = new File(fileName);
        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();
        workbook.close();
    }

    private void addStat(XSSFSheet sheet, int row, String name, double value) {
        Row r = sheet.createRow(row);
        r.createCell(0).setCellValue(name);
        r.createCell(1).setCellValue(value);
    }

    private void addStat(XSSFSheet sheet, int row, String name, int value) {
        Row r = sheet.createRow(row);
        r.createCell(0).setCellValue(name);
        r.createCell(1).setCellValue(value);
    }

    private void addBarChart(XSSFSheet sheet, int anchorRow, String title,
                             String[] categories, double[] values, String seriesName) {
        if (categories.length == 0) {
            return;
        }
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, anchorRow, 10, anchorRow + 20);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        XDDFDataSource<String> cats = XDDFDataSourcesFactory.fromArray(categories);
        Double[] vals = new Double[values.length];
        for (int i = 0; i < values.length; i++) {
            vals[i] = values[i];
        }
        XDDFNumericalDataSource<Double> nums = XDDFDataSourcesFactory.fromArray(vals);
        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        XDDFChartData.Series series = data.addSeries(cats, nums);
        series.setTitle(seriesName, null);
        chart.plot(data);
    }

    private void addTwoSeriesChart(XSSFSheet sheet, int anchorRow, String title,
                                   String[] categories, double[] values1,
                                   double[] values2, double[] values3) {
        // values1 and values3 are plotted; values2 is optional second series? 
        // For income vs expenses: values1=income, values3=spent. values2 unused.
        // For budget vs actual: values1=budget, values3=spent.
        if (categories.length == 0) {
            return;
        }
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, anchorRow, 10, anchorRow + 20);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        XDDFDataSource<String> cats = XDDFDataSourcesFactory.fromArray(categories);
        Double[] v1 = new Double[values1.length];
        Double[] v3 = new Double[values3.length];
        for (int i = 0; i < values1.length; i++) {
            v1[i] = values1[i];
            v3[i] = values3[i];
        }
        XDDFNumericalDataSource<Double> n1 = XDDFDataSourcesFactory.fromArray(v1);
        XDDFNumericalDataSource<Double> n3 = XDDFDataSourcesFactory.fromArray(v3);
        XDDFBarChartData data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        XDDFChartData.Series s1 = data.addSeries(cats, n1);
        s1.setTitle("Series 1", null);
        XDDFChartData.Series s2 = data.addSeries(cats, n3);
        s2.setTitle("Series 2", null);
        chart.plot(data);
    }

    private void addPieChart(XSSFSheet sheet, int anchorRow, String title,
                             String[] categories, double[] values) {
        if (categories.length == 0) {
            return;
        }
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, anchorRow, 10, anchorRow + 20);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        XDDFDataSource<String> cats = XDDFDataSourcesFactory.fromArray(categories);
        Double[] vals = new Double[values.length];
        for (int i = 0; i < values.length; i++) {
            vals[i] = values[i];
        }
        XDDFNumericalDataSource<Double> nums = XDDFDataSourcesFactory.fromArray(vals);
        XDDFPieChartData data = (XDDFPieChartData) chart.createData(ChartTypes.PIE, null, null);
        XDDFChartData.Series series = data.addSeries(cats, nums);
        series.setTitle("Share", null);
        chart.plot(data);
    }

    private double getValue(HashMap<String, Double> map, String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return 0;
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private String csv(String text) {
        if (text == null) {
            return "";
        }
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}