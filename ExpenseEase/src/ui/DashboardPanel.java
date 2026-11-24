package ui;

import exception.InvalidInputException;
import model.Expense;
import model.User;
import service.CategoryService;
import service.ExpenseService;
import service.IncomeService;
import service.RecurringService;
import service.ExportService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// OOP: Inheritance - DashboardPanel extends BasePanel.
// OOP: Interface implementation - ActionListener.
public class DashboardPanel extends BasePanel implements ActionListener {

    private MainFrame frame;
    private User user;
    private ExpenseService expenseService = new ExpenseService();
    private CategoryService categoryService = new CategoryService();
    private IncomeService incomeService = new IncomeService();
    private RecurringService recurringService = new RecurringService();
    private ExportService exportService = new ExportService();

    private JLabel welcomeLabel;
    private JLabel incomeValue;
    private JLabel spentValue;
    private JLabel savingsValue;
    private JLabel remainingBudgetValue;
    private DefaultTableModel tableModel;
    private JTable table;

    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton categoryButton;
    private JButton incomeButton;
    private JButton extraIncomeButton;
    private JButton recurringButton;
    private JButton budgetButton;
    private JButton exportButton;
    private JButton statsButton;
    private JButton reportButton;
    private JButton deletedButton;
    private JButton settingsButton;
    private JButton logoutButton;

    private JTextField searchField;
    private JComboBox<String> categoryFilterBox;
    private JComboBox<String> monthFilterBox;
    private JComboBox<String> sortBox;
    private JComboBox<String> orderBox;

    private String currentKeyword = "";
    private String currentCategory = "All";
    private String currentMonth = "All";
    private String currentSort = "Date";
    private boolean currentAscending = false;

    // Builds the dashboard layout.
    public DashboardPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        add(createTopBar(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public void resetForm() {
        welcomeLabel.setText("Welcome, " + user.getUsername());
        loadFilterOptions();
        refreshDashboard();
    }

    public void startSession() {
        try {
            recurringService.processDue(user.getUserId());
            if (expenseService.hasOldData(user.getUserId())) {
                int answer = JOptionPane.showConfirmDialog(this,
                        "Some old data will be deleted. Export it first?",
                        "Old data", JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION && !exportCsv()) {
                    JOptionPane.showMessageDialog(this,
                            "Old data was not deleted because the export was cancelled.");
                    refreshDashboard();
                    return;
                }
                expenseService.deleteOldData(user.getUserId());
                JOptionPane.showMessageDialog(this, "Old data was deleted.");
            }
            checkIncome();
            refreshDashboard();
        } catch (SQLException e) {
            showDatabaseError(e);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(AppColors.HEADER);
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));
        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(AppColors.TEXT);
        bar.add(welcomeLabel, BorderLayout.WEST);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nav.setBackground(AppColors.HEADER);
        statsButton = createNavButton("Statistics");
        reportButton = createNavButton("Reports");
        deletedButton = createNavButton("Deleted");
        settingsButton = createNavButton("Settings");
        logoutButton = createButton("Logout", AppColors.PRIMARY);
        statsButton.addActionListener(this);
        reportButton.addActionListener(this);
        deletedButton.addActionListener(this);
        settingsButton.addActionListener(this);
        logoutButton.addActionListener(this);
        nav.add(statsButton);
        nav.add(reportButton);
        nav.add(deletedButton);
        nav.add(settingsButton);
        nav.add(logoutButton);
        bar.add(nav, BorderLayout.EAST);
        return bar;
    }

    private JButton createNavButton(String text) {
        JButton button = createButton(text, AppColors.PRIMARY);
        return button;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBackground(AppColors.PAGE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.add(createCards(), BorderLayout.NORTH);
        JPanel middle = new JPanel(new BorderLayout(0, 10));
        middle.setBackground(AppColors.PAGE);
        middle.add(createFilterPanel(), BorderLayout.NORTH);
        middle.add(createTablePanel(), BorderLayout.CENTER);
        content.add(middle, BorderLayout.CENTER);
        content.add(createButtonBar(), BorderLayout.SOUTH);
        return content;
    }

    private JPanel createCards() {
        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 0));
        cards.setBackground(AppColors.PAGE);
        incomeValue = createValueLabel();
        spentValue = createValueLabel();
        savingsValue = createValueLabel();
        remainingBudgetValue = createValueLabel();
        cards.add(createSummaryCard("TOTAL INCOME", incomeValue));
        cards.add(createSummaryCard("TOTAL SPENT", spentValue));
        cards.add(createSummaryCard("SAVINGS", savingsValue));
        cards.add(createSummaryCard("REMAINING BUDGET", remainingBudgetValue));
        return cards;
    }

    private JPanel createSummaryCard(String title, JLabel value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(AppColors.CARD);
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(AppColors.TEXT);
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(value);
        return card;
    }

    private JLabel createValueLabel() {
        JLabel label = new JLabel("Rs. 0.00");
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setForeground(AppColors.TEXT);
        return label;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setBackground(AppColors.PAGE);
        searchField = new JTextField(18);
        searchField.setFont(FONT);
        categoryFilterBox = new JComboBox<>();
        categoryFilterBox.addItem("All");
        monthFilterBox = new JComboBox<>();
        monthFilterBox.addItem("All");
        sortBox = new JComboBox<>(new String[]{"Date", "Amount"});
        orderBox = new JComboBox<>(new String[]{"Newest first", "Oldest first",
                "High to low", "Low to high"});
        JButton applyButton = createButton("Apply", AppColors.PRIMARY);
        applyButton.addActionListener(e -> applyFilters());
        panel.add(createLabel("Search:"));
        panel.add(searchField);
        panel.add(createLabel("Category:"));
        panel.add(categoryFilterBox);
        panel.add(createLabel("Month:"));
        panel.add(monthFilterBox);
        panel.add(createLabel("Sort:"));
        panel.add(sortBox);
        panel.add(orderBox);
        panel.add(applyButton);
        return panel;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"ID", "Date", "Category", "Amount", "Description"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setFont(FONT);
        table.setRowHeight(28);
        table.setBackground(AppColors.CARD);
        table.setForeground(AppColors.TEXT);
        table.setSelectionBackground(AppColors.TABLE_SELECTED);
        table.setSelectionForeground(AppColors.TEXT);
        table.setGridColor(new Color(0xE0E0E0));
        table.setDefaultEditor(Object.class, null);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setBackground(AppColors.TABLE_HEADER);
        table.getTableHeader().setForeground(AppColors.TEXT);
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(AppColors.CARD);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xC8E6C9)));
        return scroll;
    }

    private JPanel createButtonBar() {
        JPanel bar = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(AppColors.PAGE);
        addButton = createButton("Add Expense", AppColors.PRIMARY);
        editButton = createButton("Edit Expense", AppColors.PRIMARY);
        deleteButton = createButton("Delete Expense", AppColors.PRIMARY);
        categoryButton = createButton("Add Category", AppColors.PRIMARY);
        incomeButton = createButton("Monthly Income", AppColors.PRIMARY);
        extraIncomeButton = createButton("Extra Income", AppColors.PRIMARY);
        recurringButton = createButton("Recurring", AppColors.PRIMARY);
        budgetButton = createButton("Set Budget", AppColors.PRIMARY);
        exportButton = createButton("Export", AppColors.PRIMARY);
        JButton[] buttons = {addButton, editButton, deleteButton, categoryButton,
                incomeButton, extraIncomeButton, recurringButton, budgetButton,
                exportButton};
        for (JButton button : buttons) {
            button.addActionListener(this);
            bar.add(button);
        }
        return bar;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == logoutButton) {
            frame.logout();
        } else if (e.getSource() == statsButton) {
            frame.showStatistics(user);
        } else if (e.getSource() == reportButton) {
            frame.showReport(user);
        } else if (e.getSource() == deletedButton) {
            frame.showDeleted(user);
        } else if (e.getSource() == settingsButton) {
            frame.showSettings(user);
        } else if (e.getSource() == addButton) {
            openExpenseDialog(null);
        } else if (e.getSource() == editButton) {
            editSelected();
        } else if (e.getSource() == deleteButton) {
            deleteSelected();
        } else if (e.getSource() == categoryButton) {
            addCategory();
        } else if (e.getSource() == incomeButton) {
            updateIncome();
        } else if (e.getSource() == extraIncomeButton) {
            addExtraIncome();
        } else if (e.getSource() == recurringButton) {
            openRecurringManager();
        } else if (e.getSource() == budgetButton) {
            setBudget();
        } else if (e.getSource() == exportButton) {
            exportCsv();
        }
    }

    private void loadFilterOptions() {
        try {
            categoryFilterBox.removeAllItems();
            categoryFilterBox.addItem("All");
            for (model.Category category : categoryService.listCategories(user.getUserId())) {
                categoryFilterBox.addItem(category.getCategoryName());
            }
            monthFilterBox.removeAllItems();
            monthFilterBox.addItem("All");
            ArrayList<String> months = new ArrayList<>();
            HashMap<String, Double> map = expenseService.getMonthlySpentMap(user.getUserId());
            for (String month : map.keySet()) {
                months.add(month);
            }
            HashMap<String, Double> incomeMap = incomeService.getMonthlyIncomeMap(user.getUserId());
            for (String month : incomeMap.keySet()) {
                if (!months.contains(month)) {
                    months.add(month);
                }
            }
            java.util.Collections.sort(months);
            java.util.Collections.reverse(months);
            for (String month : months) {
                monthFilterBox.addItem(month);
            }
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void applyFilters() {
        currentKeyword = searchField.getText().trim();
        currentCategory = String.valueOf(categoryFilterBox.getSelectedItem());
        if (currentCategory == null) {
            currentCategory = "All";
        }
        currentMonth = String.valueOf(monthFilterBox.getSelectedItem());
        if (currentMonth == null) {
            currentMonth = "All";
        }
        currentSort = String.valueOf(sortBox.getSelectedItem());
        String order = String.valueOf(orderBox.getSelectedItem());
        if ("Oldest first".equals(order) || "Low to high".equals(order)) {
            currentAscending = true;
        } else {
            currentAscending = false;
        }
        if ("Amount".equals(currentSort)) {
            if ("Low to high".equals(order)) {
                currentAscending = true;
            } else {
                currentAscending = false;
            }
        }
        refreshDashboard();
    }

    private int getCategoryIdByName(String name) {
        if (name == null || name.equals("All")) {
            return 0;
        }
        try {
            for (model.Category category : categoryService.listCategories(user.getUserId())) {
                if (category.getCategoryName().equals(name)) {
                    return category.getCategoryId();
                }
            }
        } catch (SQLException e) {
            showDatabaseError(e);
        }
        return 0;
    }

    private void openExpenseDialog(Expense expense) {
        ExpenseDialog dialog = new ExpenseDialog(frame, this, user, expense);
        dialog.setVisible(true);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an expense row first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            Expense expense = expenseService.findExpense(user.getUserId(), id);
            if (expense != null) {
                openExpenseDialog(expense);
            }
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an expense row first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int answer = JOptionPane.showConfirmDialog(this, "Delete this expense?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            expenseService.deleteExpense(user.getUserId(), id);
            refreshDashboard();
            JOptionPane.showMessageDialog(this, "Expense deleted.");
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "New category name:");
        if (name == null) {
            return;
        }
        try {
            categoryService.addCustomCategory(user.getUserId(), name);
            JOptionPane.showMessageDialog(this, "Category added.");
            loadFilterOptions();
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void updateIncome() {
        String month = getCurrentMonth();
        try {
            double oldIncome = incomeService.getIncome(user.getUserId(), month);
            String text = JOptionPane.showInputDialog(this,
                    "Income for " + month + " (current Rs. " + oldIncome + "):",
                    "Monthly income", JOptionPane.QUESTION_MESSAGE);
            if (text == null) {
                return;
            }
            double amount = Double.parseDouble(text.trim());
            incomeService.setMonthlyIncome(user.getUserId(), month, amount);
            refreshDashboard();
            JOptionPane.showMessageDialog(this, "Income saved.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void addExtraIncome() {
        try {
            String amountText = JOptionPane.showInputDialog(this, "Amount:");
            if (amountText == null) {
                return;
            }
            double amount = Double.parseDouble(amountText.trim());
            String type = JOptionPane.showInputDialog(this, "Income type (Bonus, Gift, etc.):");
            if (type == null) {
                return;
            }
            String description = JOptionPane.showInputDialog(this, "Short description:");
            if (description == null) {
                description = type;
            }
            String date = CalendarDialog.showDialog(this);
            if (date == null) {
                date = getToday();
            }
            incomeService.addExtraIncome(user.getUserId(), amount, date, type, description);
            refreshDashboard();
            JOptionPane.showMessageDialog(this, "Extra income saved.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void openRecurringManager() {
        RecurringManagerDialog dialog = new RecurringManagerDialog(frame, user);
        dialog.setVisible(true);
        refreshDashboard();
    }

    private void setBudget() {
        try {
            String month = getCurrentMonth();
            double oldBudget = expenseService.getBudget(user.getUserId(), month);
            String text = JOptionPane.showInputDialog(this,
                    "Budget for " + month + " (current Rs. " + oldBudget + "):",
                    "Monthly budget", JOptionPane.QUESTION_MESSAGE);
            if (text == null) {
                return;
            }
            double amount = Double.parseDouble(text.trim());
            expenseService.setBudget(user.getUserId(), month, amount);
            refreshDashboard();
            JOptionPane.showMessageDialog(this, "Budget saved.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    public boolean exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save CSV report");
        chooser.setSelectedFile(new File("expense_report_" + user.getUsername() + ".csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        String fileName = chooser.getSelectedFile().getAbsolutePath();
        if (!fileName.toLowerCase().endsWith(".csv")) {
            fileName = fileName + ".csv";
        }
        try {
            exportService.exportCsv(user.getUserId(), user.getUsername(), fileName);
            JOptionPane.showMessageDialog(this, "CSV saved to:\n" + fileName);
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void checkIncome() throws SQLException {
        String month = getCurrentMonth();
        if (incomeService.getIncome(user.getUserId(), month) > 0) {
            return;
        }
        String text = JOptionPane.showInputDialog(this,
                "Enter your income for " + month + ":", "Monthly income",
                JOptionPane.QUESTION_MESSAGE);
        if (text == null) {
            return;
        }
        try {
            double amount = Double.parseDouble(text.trim());
            incomeService.setMonthlyIncome(user.getUserId(), month, amount);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getCurrentMonth() {
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        return today.toString().substring(0, 7);
    }

    private String getToday() {
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        return today.toString();
    }

    public void refreshDashboard() {
        try {
            int categoryId = getCategoryIdByName(currentCategory);
            ArrayList<Expense> list = expenseService.searchExpenses(
                    user.getUserId(), currentKeyword, categoryId, currentMonth,
                    currentSort, currentAscending);
            tableModel.setRowCount(0);
            for (Expense expense : list) {
                tableModel.addRow(new Object[] {
                        expense.getExpenseId(), expense.getDate(),
                        expense.getCategoryName(),
                        String.format("%.2f", expense.getAmount()),
                        expense.getDescription()
                });
            }
            double income = incomeService.getTotalIncome(user.getUserId())
                    + incomeService.getTotalExtraIncome(user.getUserId());
            double spent = expenseService.getTotalSpent(user.getUserId());
            incomeValue.setText("Rs. " + String.format("%.2f", income));
            spentValue.setText("Rs. " + String.format("%.2f", spent));
            savingsValue.setText("Rs. " + String.format("%.2f", income - spent));

            String month = getCurrentMonth();
            double budget = expenseService.getBudget(user.getUserId(), month);
            double monthSpent = 0;
            HashMap<String, Double> monthMap = expenseService.getMonthlySpentMap(user.getUserId());
            if (monthMap.containsKey(month)) {
                monthSpent = monthMap.get(month);
            }
            double remaining = budget - monthSpent;
            remainingBudgetValue.setText("Rs. " + String.format("%.2f", remaining));

            if (budget > 0) {
                double percent = (monthSpent / budget) * 100.0;
                if (percent >= 100) {
                    JOptionPane.showMessageDialog(this,
                            "You have used 100% or more of your monthly budget.");
                } else if (percent >= 80) {
                    JOptionPane.showMessageDialog(this,
                            "You have used " + String.format("%.0f", percent)
                                    + "% of your monthly budget.");
                }
            }
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    private void showDatabaseError(SQLException e) {
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}