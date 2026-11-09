package ui;

import exception.InvalidInputException;
import model.Expense;
import model.User;
import service.CategoryService;
import service.ExpenseService;
import service.IncomeService;
import service.XlsxExporter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
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
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;

// OOP: Inheritance - DashboardPanel extends BasePanel.
// OOP: Interface implementation - ActionListener.
public class DashboardPanel extends BasePanel implements ActionListener {

    private MainFrame frame;
    private User user;
    private ExpenseService expenseService = new ExpenseService();
    private CategoryService categoryService = new CategoryService();
    private IncomeService incomeService = new IncomeService();
    private XlsxExporter xlsxExporter = new XlsxExporter();
    private JLabel welcomeLabel;
    private JLabel incomeValue;
    private JLabel spentValue;
    private JLabel savingsValue;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton categoryButton;
    private JButton incomeButton;
    private JButton exportButton;
    private JButton logoutButton;

    // Builds the dashboard layout.
    public DashboardPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        add(createTopBar(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    // Remembers the user who logged in.
    public void setUser(User user) {
        this.user = user;
    }

    // OOP: Overriding the abstract method from BasePanel.
    @Override
    public void resetForm() {
        welcomeLabel.setText("Welcome, " + user.getUsername());
        refreshDashboard();
    }

    // Runs the login checks and then loads the dashboard.
    public void startSession() {
        try {
            if (expenseService.hasOldData(user.getUserId())) {
                int answer = JOptionPane.showConfirmDialog(this,
                        "Some old data will be deleted. Export it first?",
                        "Old data", JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION && !doExport()) {
                    JOptionPane.showMessageDialog(this,
                            "Old data was not deleted because the export was cancelled.");
                    return;
                }
                expenseService.deleteOldData(user.getUserId());
                JOptionPane.showMessageDialog(this, "Old data was deleted.");
            }
            checkIncome();
            refreshDashboard();
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    // Creates the top bar with the welcome text and logout button.
    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(AppColors.HEADER);
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));
        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(AppColors.TEXT);
        bar.add(welcomeLabel, BorderLayout.WEST);
        logoutButton = createButton("Logout", AppColors.PRIMARY_DARK);
        logoutButton.addActionListener(this);
        bar.add(logoutButton, BorderLayout.EAST);
        return bar;
    }

    // Creates the cards, table and button area.
    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setBackground(AppColors.PAGE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.add(createCards(), BorderLayout.NORTH);
        content.add(createTablePanel(), BorderLayout.CENTER);
        content.add(createButtonBar(), BorderLayout.SOUTH);
        return content;
    }

    // Creates the three summary cards.
    private JPanel createCards() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 15, 0));
        cards.setBackground(AppColors.PAGE);
        incomeValue = createValueLabel();
        spentValue = createValueLabel();
        savingsValue = createValueLabel();
        cards.add(createSummaryCard("TOTAL INCOME", incomeValue));
        cards.add(createSummaryCard("TOTAL SPENT", spentValue));
        cards.add(createSummaryCard("SAVINGS", savingsValue));
        return cards;
    }

    // Creates one white summary card.
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

    // Creates a big value label for a summary card.
    private JLabel createValueLabel() {
        JLabel label = new JLabel("Rs. 0.00");
        label.setFont(new Font("Segoe UI", Font.BOLD, 24));
        label.setForeground(AppColors.TEXT);
        return label;
    }

    // Creates the expense table inside a scroll pane.
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

    // Creates the row of action buttons.
    private JPanel createButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(AppColors.PAGE);
        addButton = createButton("Add Expense", AppColors.PRIMARY);
        editButton = createButton("Edit Expense", AppColors.PRIMARY);
        deleteButton = createButton("Delete Expense", AppColors.DANGER);
        categoryButton = createButton("Add Category", AppColors.PRIMARY);
        incomeButton = createButton("Set/Update Monthly Income", AppColors.PRIMARY_DARK);
        exportButton = createButton("Export to Excel", AppColors.PRIMARY_DARK);
        JButton[] buttons = {addButton, editButton, deleteButton,
                categoryButton, incomeButton, exportButton};
        for (JButton button : buttons) {
            button.addActionListener(this);
            bar.add(button);
        }
        return bar;
    }

    // OOP: Overriding actionPerformed from ActionListener.
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == logoutButton) {
            frame.logout();
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
        } else if (e.getSource() == exportButton) {
            doExport();
        }
    }

    // Opens the add or edit dialog.
    private void openExpenseDialog(Expense expense) {
        ExpenseDialog dialog = new ExpenseDialog(frame, this, user, expense);
        dialog.setVisible(true);
    }

    // Opens the edit dialog for the selected table row.
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

    // Deletes the selected row after asking for confirmation.
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

    // Adds a custom category for this user.
    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "New category name:");
        if (name == null) {
            return;
        }
        try {
            categoryService.addCustomCategory(user.getUserId(), name);
            JOptionPane.showMessageDialog(this, "Category added.");
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    // Sets or updates the income of the current month.
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

    // Asks for a file name and writes the Excel report.
    private boolean doExport() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Excel report");
        chooser.setSelectedFile(new File("expense_report_"
                + user.getUsername() + ".xlsx"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        String fileName = chooser.getSelectedFile().getAbsolutePath();
        if (!fileName.toLowerCase().endsWith(".xlsx")) {
            fileName = fileName + ".xlsx";
        }
        try {
            xlsxExporter.exportData(user.getUserId(), user.getUsername(), fileName);
            JOptionPane.showMessageDialog(this,
                    xlsxExporter.getFileType() + " saved to:\n" + fileName);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "File error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (SQLException e) {
            showDatabaseError(e);
            return false;
        }
    }

    // Asks for this month's income if it is not set yet.
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

    // Returns the current month as yyyy-MM.
    private String getCurrentMonth() {
        Date today = new Date(System.currentTimeMillis());
        return today.toString().substring(0, 7);
    }

    // Reloads the table and recalculates the three cards.
    public void refreshDashboard() {
        try {
            ArrayList<Expense> list = expenseService.listExpenses(user.getUserId());
            tableModel.setRowCount(0);
            for (Expense expense : list) {
                tableModel.addRow(new Object[] {
                        expense.getExpenseId(), expense.getDate(),
                        expense.getCategoryName(),
                        String.format("%.2f", expense.getAmount()),
                        expense.getDescription()
                });
            }
            double income = incomeService.getTotalIncome(user.getUserId());
            double spent = expenseService.getTotalSpent(user.getUserId());
            incomeValue.setText("Rs. " + String.format("%.2f", income));
            spentValue.setText("Rs. " + String.format("%.2f", spent));
            savingsValue.setText("Rs. " + String.format("%.2f", income - spent));
        } catch (SQLException e) {
            showDatabaseError(e);
        }
    }

    // Shows one common database error message.
    private void showDatabaseError(SQLException e) {
        JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
