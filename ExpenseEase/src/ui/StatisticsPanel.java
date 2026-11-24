package ui;

import model.User;
import service.ExpenseService;
import service.IncomeService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Statistics dashboard with charts and insight sentences.
public class StatisticsPanel extends BasePanel {

    private MainFrame frame;
    private User user;
    private ExpenseService expenseService = new ExpenseService();
    private IncomeService incomeService = new IncomeService();

    private JComboBox<String> monthBox;
    private JLabel totalIncomeLabel;
    private JLabel totalExpenseLabel;
    private JLabel savingsLabel;
    private JLabel highestCategoryLabel;
    private JLabel lowestCategoryLabel;
    private JLabel highestDayLabel;
    private JLabel avgMonthlyLabel;
    private JLabel avgDailyLabel;
    private JLabel expenseCountLabel;
    private JLabel monthCountLabel;
    private JTextArea insightArea;

    private SimpleChartPanel monthlyExpenditureChart;
    private SimpleChartPanel incomeVsExpenseChart;
    private SimpleChartPanel categoryBarChart;
    private SimpleChartPanel categoryPieChart;
    private SimpleChartPanel savingsChart;
    private SimpleChartPanel budgetChart;

    private List<String> months = new ArrayList<>();
    private HashMap<String, Double> incomeMap = new HashMap<>();
    private HashMap<String, Double> extraMap = new HashMap<>();
    private HashMap<String, Double> spentMap = new HashMap<>();
    private HashMap<String, Double> categoryTotals = new HashMap<>();
    private List<model.Expense> allExpenses = new ArrayList<>();

    public StatisticsPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        add(createTopBar(), BorderLayout.NORTH);
        add(createCenter(), BorderLayout.CENTER);
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void resetForm() {
        loadMonths();
        refreshStats();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(AppColors.HEADER);
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel("Statistics Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(AppColors.TEXT);
        bar.add(title, BorderLayout.WEST);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(AppColors.HEADER);
        right.add(createLabel("Month:"));
        monthBox = new JComboBox<>();
        monthBox.addItem("Overall");
        monthBox.addActionListener(e -> refreshStats());
        right.add(monthBox);
        JButton back = createButton("Back to Dashboard", AppColors.PRIMARY_DARK);
        back.addActionListener(e -> frame.showDashboard(user));
        right.add(back);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel createCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(AppColors.PAGE);
        center.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel summary = new JPanel(new GridLayout(2, 5, 10, 10));
        summary.setBackground(AppColors.PAGE);
        totalIncomeLabel = createMetricLabel();
        totalExpenseLabel = createMetricLabel();
        savingsLabel = createMetricLabel();
        highestCategoryLabel = createMetricLabel();
        lowestCategoryLabel = createMetricLabel();
        highestDayLabel = createMetricLabel();
        avgMonthlyLabel = createMetricLabel();
        avgDailyLabel = createMetricLabel();
        expenseCountLabel = createMetricLabel();
        monthCountLabel = createMetricLabel();
        summary.add(wrapMetric("Total Income", totalIncomeLabel));
        summary.add(wrapMetric("Total Expenses", totalExpenseLabel));
        summary.add(wrapMetric("Savings", savingsLabel));
        summary.add(wrapMetric("Highest Category", highestCategoryLabel));
        summary.add(wrapMetric("Lowest Category", lowestCategoryLabel));
        summary.add(wrapMetric("Highest Day", highestDayLabel));
        summary.add(wrapMetric("Avg Monthly", avgMonthlyLabel));
        summary.add(wrapMetric("Avg Daily", avgDailyLabel));
        summary.add(wrapMetric("Expense Count", expenseCountLabel));
        summary.add(wrapMetric("Months", monthCountLabel));
        center.add(summary, BorderLayout.NORTH);

        JPanel charts = new JPanel(new GridLayout(2, 3, 10, 10));
        charts.setBackground(AppColors.PAGE);
        monthlyExpenditureChart = new SimpleChartPanel("Monthly Expenditure", 1);
        incomeVsExpenseChart = new SimpleChartPanel("Income vs Expenses", 2);
        categoryBarChart = new SimpleChartPanel("Category Spending", 1);
        categoryPieChart = new SimpleChartPanel("Category Share", 3);
        savingsChart = new SimpleChartPanel("Monthly Savings", 1);
        budgetChart = new SimpleChartPanel("Budget vs Actual", 2);
        charts.add(monthlyExpenditureChart);
        charts.add(incomeVsExpenseChart);
        charts.add(categoryBarChart);
        charts.add(categoryPieChart);
        charts.add(savingsChart);
        charts.add(budgetChart);
        center.add(charts, BorderLayout.CENTER);

        insightArea = new JTextArea(6, 40);
        insightArea.setFont(FONT);
        insightArea.setEditable(false);
        insightArea.setLineWrap(true);
        insightArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(insightArea);
        scroll.setPreferredSize(new Dimension(0, 120));
        center.add(scroll, BorderLayout.SOUTH);
        return center;
    }

    private JLabel createMetricLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(AppColors.TEXT);
        return label;
    }

    private JPanel wrapMetric(String title, JLabel value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(AppColors.CARD);
        panel.setBorder(new EmptyBorder(8, 10, 8, 10));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.setForeground(AppColors.TEXT);
        panel.add(t);
        panel.add(value);
        return panel;
    }

    private void loadMonths() {
        monthBox.removeAllItems();
        monthBox.addItem("Overall");
        try {
            incomeMap = incomeService.getMonthlyIncomeMap(user.getUserId());
            extraMap = incomeService.getMonthlyExtraIncomeMap(user.getUserId());
            spentMap = expenseService.getMonthlySpentMap(user.getUserId());
            allExpenses = expenseService.listExpenses(user.getUserId());
            java.util.Set<String> set = new java.util.TreeSet<>();
            set.addAll(incomeMap.keySet());
            set.addAll(extraMap.keySet());
            set.addAll(spentMap.keySet());
            months = new ArrayList<>(set);
            java.util.Collections.sort(months);
            java.util.Collections.reverse(months);
            for (String month : months) {
                monthBox.addItem(month);
            }
            categoryTotals = expenseService.getCategoryTotals(user.getUserId(), "All");
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void refreshStats() {
        try {
            String selected = String.valueOf(monthBox.getSelectedItem());
            if (selected == null) {
                selected = "Overall";
            }

            double totalIncome = incomeService.getTotalIncome(user.getUserId())
                    + incomeService.getTotalExtraIncome(user.getUserId());
            double totalSpent = expenseService.getTotalSpent(user.getUserId());
            totalIncomeLabel.setText("Rs. " + String.format("%.2f", totalIncome));
            totalExpenseLabel.setText("Rs. " + String.format("%.2f", totalSpent));
            savingsLabel.setText("Rs. " + String.format("%.2f", totalIncome - totalSpent));

            // Category totals for selected.
            HashMap<String, Double> catTotals;
            if ("Overall".equals(selected)) {
                catTotals = expenseService.getCategoryTotals(user.getUserId(), "All");
            } else {
                catTotals = expenseService.getCategoryTotals(user.getUserId(), selected);
            }
            String highestCat = "-";
            String lowestCat = "-";
            double highestVal = -1;
            double lowestVal = Double.MAX_VALUE;
            boolean hasLowest = false;
            for (Map.Entry<String, Double> entry : catTotals.entrySet()) {
                if (entry.getValue() > highestVal) {
                    highestVal = entry.getValue();
                    highestCat = entry.getKey();
                }
                if (entry.getValue() < lowestVal) {
                    lowestVal = entry.getValue();
                    lowestCat = entry.getKey();
                    hasLowest = true;
                }
            }
            if (catTotals.isEmpty()) {
                highestCat = "-";
                lowestCat = "-";
            }
            highestCategoryLabel.setText(highestCat);
            lowestCategoryLabel.setText(hasLowest ? lowestCat : "-");

            // Highest spending day.
            String highestDay = "-";
            HashMap<String, Double> dayMap = new HashMap<>();
            for (model.Expense expense : allExpenses) {
                if (!"Overall".equals(selected) && !expense.getDate().startsWith(selected)) {
                    continue;
                }
                double old = dayMap.containsKey(expense.getDate()) ? dayMap.get(expense.getDate()) : 0;
                dayMap.put(expense.getDate(), old + expense.getAmount());
            }
            double maxDay = -1;
            for (Map.Entry<String, Double> entry : dayMap.entrySet()) {
                if (entry.getValue() > maxDay) {
                    maxDay = entry.getValue();
                    highestDay = entry.getKey();
                }
            }
            highestDayLabel.setText(highestDay);

            // Average monthly and daily.
            double avgMonthly = 0;
            double avgDaily = 0;
            if (!spentMap.isEmpty()) {
                double sum = 0;
                for (double v : spentMap.values()) {
                    sum = sum + v;
                }
                avgMonthly = sum / spentMap.size();
            }
            if (!allExpenses.isEmpty()) {
                double sum = 0;
                for (model.Expense expense : allExpenses) {
                    sum = sum + expense.getAmount();
                }
                // distinct days
                java.util.Set<String> days = new java.util.HashSet<>();
                for (model.Expense expense : allExpenses) {
                    days.add(expense.getDate());
                }
                if (!days.isEmpty()) {
                    avgDaily = sum / days.size();
                }
            }
            avgMonthlyLabel.setText("Rs. " + String.format("%.2f", avgMonthly));
            avgDailyLabel.setText("Rs. " + String.format("%.2f", avgDaily));

            // Expense count and months.
            int count = 0;
            for (model.Expense expense : allExpenses) {
                if ("Overall".equals(selected) || expense.getDate().startsWith(selected)) {
                    count++;
                }
            }
            expenseCountLabel.setText(String.valueOf(count));
            monthCountLabel.setText(String.valueOf(spentMap.size()));

            // Monthly stats for selected month.
            if (!"Overall".equals(selected)) {
                double monthIncome = (incomeMap.containsKey(selected) ? incomeMap.get(selected) : 0)
                        + (extraMap.containsKey(selected) ? extraMap.get(selected) : 0);
                double monthSpent = spentMap.containsKey(selected) ? spentMap.get(selected) : 0;
                // Could add more detailed labels but keep summary.
            }

            // Budget usage for selected month.
            double budget = 0;
            if (!"Overall".equals(selected)) {
                budget = expenseService.getBudget(user.getUserId(), selected);
            }

            // Savings goal progress.
            double goal = expenseService.getSavingsGoal(user.getUserId());

            // Insights.
            StringBuilder insights = new StringBuilder();
            // Current vs previous month.
            if (!"Overall".equals(selected)) {
                // find previous month in sorted months
            }
            // General insights.
            if (!catTotals.isEmpty() && highestCat != null && !"-".equals(highestCat)) {
                insights.append("Highest spending category: ").append(highestCat).append(".\n");
                if (totalSpent > 0) {
                    double pct = (catTotals.get(highestCat) / totalSpent) * 100.0;
                    insights.append(highestCat).append(" accounts for ")
                            .append(String.format("%.0f", pct))
                            .append("% of total expenses.\n");
                }
            }
            if (highestDay != null && !"-".equals(highestDay)) {
                insights.append("You spent the most money on ").append(highestDay).append(".\n");
            }
            if (budget > 0 && !"Overall".equals(selected)) {
                double monthSpent = spentMap.containsKey(selected) ? spentMap.get(selected) : 0;
                double percent = (monthSpent / budget) * 100.0;
                insights.append("You have used ").append(String.format("%.0f", percent))
                        .append("% of your monthly budget.\n");
            }
            if (goal > 0) {
                double progress = (totalIncome - totalSpent) / goal * 100.0;
                if (progress < 0) {
                    progress = 0;
                }
                insights.append("Savings goal progress: ")
                        .append(String.format("%.0f", progress)).append("%.\n");
            }
            // Current vs previous month comparison.
            if (months.size() >= 2) {
                // months sorted descending
                String current = months.get(0);
                String previous = months.get(1);
                double currentSpent = spentMap.containsKey(current) ? spentMap.get(current) : 0;
                double previousSpent = spentMap.containsKey(previous) ? spentMap.get(previous) : 0;
                if (previousSpent > 0) {
                    double change = ((currentSpent - previousSpent) / previousSpent) * 100.0;
                    if (change >= 0) {
                        insights.append("Your spending increased by ")
                                .append(String.format("%.0f", change))
                                .append("% compared to last month.\n");
                    } else {
                        insights.append("Your spending decreased by ")
                                .append(String.format("%.0f", -change))
                                .append("% compared to last month.\n");
                    }
                }
                double currentIncome = (incomeMap.containsKey(current) ? incomeMap.get(current) : 0)
                        + (extraMap.containsKey(current) ? extraMap.get(current) : 0);
                double previousIncome = (incomeMap.containsKey(previous) ? incomeMap.get(previous) : 0)
                        + (extraMap.containsKey(previous) ? extraMap.get(previous) : 0);
                if (previousIncome > 0) {
                    double change = ((currentIncome - previousIncome) / previousIncome) * 100.0;
                    if (change >= 0) {
                        insights.append("Your income increased compared to the previous month.\n");
                    } else {
                        insights.append("Your income decreased compared to the previous month.\n");
                    }
                }
                double currentSavings = currentIncome - currentSpent;
                double previousSavings = previousIncome - previousSpent;
                if (currentSavings > previousSavings) {
                    insights.append("Your savings increased compared to the previous month.\n");
                } else if (currentSavings < previousSavings) {
                    insights.append("Your savings decreased compared to the previous month.\n");
                }
            }
            // Month with highest and lowest expenses.
            if (!spentMap.isEmpty()) {
                String maxMonth = null;
                double maxVal = -1;
                String minMonth = null;
                double minVal = Double.MAX_VALUE;
                for (Map.Entry<String, Double> entry : spentMap.entrySet()) {
                    if (entry.getValue() > maxVal) {
                        maxVal = entry.getValue();
                        maxMonth = entry.getKey();
                    }
                    if (entry.getValue() < minVal) {
                        minVal = entry.getValue();
                        minMonth = entry.getKey();
                    }
                }
                if (maxMonth != null) {
                    insights.append("Your highest expenditure occurred in ").append(maxMonth).append(".\n");
                }
                if (minMonth != null) {
                    insights.append("Your lowest expenditure occurred in ").append(minMonth).append(".\n");
                }
            }

            // Month with highest income and savings.
            if (!incomeMap.isEmpty()) {
                String maxIncomeMonth = null;
                double maxIncome = -1;
                for (String month : incomeMap.keySet()) {
                    double value = incomeMap.get(month)
                            + (extraMap.containsKey(month) ? extraMap.get(month) : 0);
                    if (value > maxIncome) {
                        maxIncome = value;
                        maxIncomeMonth = month;
                    }
                }
                if (maxIncomeMonth != null) {
                    insights.append("Month with highest income: ").append(maxIncomeMonth).append(".\n");
                }
            }
            if (!spentMap.isEmpty()) {
                String maxSavingsMonth = null;
                double maxSavings = Double.MIN_VALUE;
                for (String month : spentMap.keySet()) {
                    double income = (incomeMap.containsKey(month) ? incomeMap.get(month) : 0)
                            + (extraMap.containsKey(month) ? extraMap.get(month) : 0);
                    double savings = income - (spentMap.containsKey(month) ? spentMap.get(month) : 0);
                    if (savings > maxSavings) {
                        maxSavings = savings;
                        maxSavingsMonth = month;
                    }
                }
                if (maxSavingsMonth != null) {
                    insights.append("Month with highest savings: ").append(maxSavingsMonth).append(".\n");
                }
            }
            insightArea.setText(insights.toString());

            this.categoryTotals = catTotals;

            // Update charts.
            updateCharts(selected);

        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void updateCharts(String selected) {
        // Monthly expenditure chart data (all months ascending).
        ArrayList<String> ascMonths = new ArrayList<>(months);
        Collections.sort(ascMonths);
        String[] monthLabels = ascMonths.toArray(new String[0]);
        double[] spentVals = new double[monthLabels.length];
        double[] incomeVals = new double[monthLabels.length];
        double[] extraVals = new double[monthLabels.length];
        double[] savingsVals = new double[monthLabels.length];
        double[] budgetVals = new double[monthLabels.length];
        for (int i = 0; i < monthLabels.length; i++) {
            spentVals[i] = spentMap.containsKey(monthLabels[i]) ? spentMap.get(monthLabels[i]) : 0;
            incomeVals[i] = incomeMap.containsKey(monthLabels[i]) ? incomeMap.get(monthLabels[i]) : 0;
            extraVals[i] = extraMap.containsKey(monthLabels[i]) ? extraMap.get(monthLabels[i]) : 0;
            savingsVals[i] = incomeVals[i] + extraVals[i] - spentVals[i];
            try {
                budgetVals[i] = expenseService.getBudget(user.getUserId(), monthLabels[i]);
            } catch (SQLException e) {
                budgetVals[i] = 0;
            }
        }
        monthlyExpenditureChart.setData("Monthly Expenditure", monthLabels, spentVals, null, 1);
        incomeVsExpenseChart.setData("Income vs Expenses", monthLabels, incomeVals, spentVals, 2);
        savingsChart.setData("Monthly Savings", monthLabels, savingsVals, null, 1);
        budgetChart.setData("Budget vs Actual", monthLabels, budgetVals, spentVals, 2);

        // Category charts.
        String[] catLabels = categoryTotals.keySet().toArray(new String[0]);
        java.util.Arrays.sort(catLabels);
        double[] catVals = new double[catLabels.length];
        for (int i = 0; i < catLabels.length; i++) {
            catVals[i] = categoryTotals.get(catLabels[i]);
        }
        categoryBarChart.setData("Category Spending", catLabels, catVals, null, 1);
        categoryPieChart.setData("Category Share", catLabels, catVals, null, 3);
    }

    private void showDatabaseError(SQLException e) {
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Generic chart panel.
    private class SimpleChartPanel extends JPanel {
        private String title = "";
        private int type = 1;
        private String[] labels = new String[0];
        private double[] v1 = new double[0];
        private double[] v2 = new double[0];

        SimpleChartPanel(String title, int type) {
            this.title = title;
            this.type = type;
            setBackground(AppColors.CARD);
            setBorder(BorderFactory.createLineBorder(new Color(0xC8E6C9)));
        }

        void setData(String title, String[] labels, double[] v1, double[] v2, int type) {
            this.title = title;
            this.labels = labels;
            this.v1 = v1;
            this.v2 = v2;
            this.type = type;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            int w = getWidth();
            int h = getHeight();
            g2.setColor(AppColors.TEXT);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
            g2.drawString(title, 10, 18);
            if (labels == null || labels.length == 0) {
                g2.setFont(FONT);
                g2.drawString("No data", 10, 40);
                return;
            }
            if (type == 3) {
                drawPie(g2, w, h);
            } else {
                drawBars(g2, w, h);
            }
        }

        private static final Color[] CHART_COLORS = {
                new Color(0x42A5F5), new Color(0xFFA726), new Color(0x66BB6A),
                new Color(0xAB47BC), new Color(0xEF5350), new Color(0x26C6DA),
                new Color(0xFFCA28), new Color(0x8D6E63), new Color(0x78909C),
                new Color(0xEC407A)
        };

        private void drawBars(Graphics2D g2, int w, int h) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int left = 65;
            int top = 30;
            int bottom = h - 120;
            int right = w - 15;
            int chartHeight = bottom - top;
            double max = 0;
            for (double v : v1) {
                if (v > max) {
                    max = v;
                }
            }
            if (v2 != null) {
                for (double v : v2) {
                    if (v > max) {
                        max = v;
                    }
                }
            }
            if (max <= 0) {
                max = 1;
            }
            int n = labels.length;
            int slot = (right - left) / Math.max(n, 1);
            int barWidth = Math.max(6, slot / (type == 2 ? 3 : 2));
            Color color1 = (type == 2) ? new Color(0x42A5F5) : null;
            Color color2 = (type == 2) ? new Color(0xFFA726) : null;

            // Y-axis scale near the graph.
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            int ticks = 5;
            for (int t = 0; t <= ticks; t++) {
                double val = (max / ticks) * t;
                int yy = bottom - (int) ((val / max) * chartHeight);
                g2.drawLine(left - 5, yy, left, yy);
                String valStr = String.format("%.0f", val);
                g2.drawString(valStr, left - 40, yy + 4);
                if (t > 0 && t < ticks) {
                    g2.setColor(new Color(0xDDDDDD));
                    g2.drawLine(left, yy, right, yy);
                    g2.setColor(Color.GRAY);
                }
            }
            g2.drawString("0", left - 40, bottom + 4);

            for (int i = 0; i < n; i++) {
                int groupWidth = (type == 2) ? (barWidth * 2 + 2) : barWidth;
                int x = left + i * slot + (slot - groupWidth) / 2;
                int barH1 = (int) ((v1[i] / max) * chartHeight);
                g2.setColor(color1 != null ? color1 : CHART_COLORS[i % CHART_COLORS.length]);
                g2.fillRect(x, bottom - barH1, barWidth, barH1);

                if (v2 != null && v2.length == n) {
                    int barH2 = (int) ((v2[i] / max) * chartHeight);
                    g2.setColor(color2 != null ? color2 : CHART_COLORS[(i + 1) % CHART_COLORS.length]);
                    g2.fillRect(x + barWidth + 2, bottom - barH2, barWidth, barH2);
                }

                // Draw full x-axis label vertically (not slanted) below the axis.
                g2.setColor(AppColors.TEXT);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                AffineTransform oldTransform = g2.getTransform();
                int labelX = x + barWidth / 2;
                g2.translate(labelX, bottom + 8);
                g2.rotate(Math.PI / 2);
                String label = (labels[i] == null) ? "" : labels[i];
                g2.drawString(label, 0, 0);
                g2.setTransform(oldTransform);
            }
            g2.setColor(Color.GRAY);
            g2.drawLine(left, bottom, right, bottom);
            g2.drawLine(left, top, left, bottom);
        }

        private void drawPie(Graphics2D g2, int w, int h) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            double total = 0;
            for (double v : v1) {
                total = total + v;
            }
            if (total <= 0) {
                g2.drawString("No data", 10, 40);
                return;
            }
            int size = Math.min(w, h) - 70;
            int x = (w - size) / 2;
            int y = 30;
            double start = 0;
            for (int i = 0; i < v1.length; i++) {
                double angle = (v1[i] / total) * 360.0;
                g2.setColor(CHART_COLORS[i % CHART_COLORS.length]);
                g2.fillArc(x, y, size, size, (int) start, (int) Math.ceil(angle));
                start = start + angle;
            }
        }
    }
}