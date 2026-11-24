package ui;

import model.User;
import service.ExpenseService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

// Shows deleted expenses and allows restore.
public class DeletedPanel extends BasePanel {

    private MainFrame frame;
    private User user;
    private ExpenseService expenseService = new ExpenseService();
    private DefaultTableModel tableModel;
    private JTable table;

    public DeletedPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        add(createTopBar(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonBar(), BorderLayout.SOUTH);
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void resetForm() {
        refreshTable();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(AppColors.HEADER);
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel("Deleted Expenses History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(AppColors.TEXT);
        bar.add(title, BorderLayout.WEST);
        JButton back = createButton("Back to Dashboard", AppColors.PRIMARY_DARK);
        back.addActionListener(e -> frame.showDashboard(user));
        bar.add(back, BorderLayout.EAST);
        return bar;
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"Log ID", "Expense ID", "Category", "Amount", "Date",
                "Description", "Deleted At", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setFont(FONT);
        table.setRowHeight(26);
        table.setDefaultEditor(Object.class, null);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xC8E6C9)));
        return scroll;
    }

    private JPanel createButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(AppColors.PAGE);
        JButton restoreButton = createButton("Restore", AppColors.PRIMARY);
        JButton refreshButton = createButton("Refresh", AppColors.PRIMARY_DARK);
        restoreButton.addActionListener(e -> restoreSelected());
        refreshButton.addActionListener(e -> refreshTable());
        bar.add(restoreButton);
        bar.add(refreshButton);
        return bar;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            ArrayList<HashMap<String, String>> logs = expenseService.getDeletedLogs(user.getUserId());
            for (HashMap<String, String> row : logs) {
                tableModel.addRow(new Object[] {
                        Integer.parseInt(row.get("logId")),
                        Integer.parseInt(row.get("expenseId")),
                        row.get("categoryName"),
                        row.get("amount"),
                        row.get("date"),
                        row.get("description"),
                        row.get("deletedAt"),
                        row.get("status")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void restoreSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }
        int logId = (int) tableModel.getValueAt(row, 0);
        String status = String.valueOf(tableModel.getValueAt(row, 7));
        if ("RESTORED".equals(status)) {
            JOptionPane.showMessageDialog(this, "This expense was already restored.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Restore this expense?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            expenseService.restoreDeletedExpense(user.getUserId(), logId);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Expense restored.");
        } catch (exception.InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}