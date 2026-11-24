package ui;

import model.User;
import service.ExpenseService;
import service.IncomeService;

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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

// Month-wise report screen.
public class ReportPanel extends BasePanel {

    private MainFrame frame;
    private User user;
    private ExpenseService expenseService = new ExpenseService();
    private IncomeService incomeService = new IncomeService();
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel detailLabel;

    public ReportPanel(MainFrame frame) {
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
        refreshReport();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(AppColors.HEADER);
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel("Month-wise Report");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(AppColors.TEXT);
        bar.add(title, BorderLayout.WEST);
        JButton back = createButton("Back to Dashboard", AppColors.PRIMARY_DARK);
        back.addActionListener(e -> frame.showDashboard(user));
        bar.add(back, BorderLayout.EAST);
        return bar;
    }

    private JPanel createCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(AppColors.PAGE);
        center.setBorder(new EmptyBorder(20, 20, 20, 20));
        String[] columns = {"Month", "Total Income", "Extra Income", "Total Expenses",
                "Savings", "Budget", "Budget Usage %"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setFont(FONT);
        table.setRowHeight(26);
        table.setDefaultEditor(Object.class, null);
        JScrollPane scroll = new JScrollPane(table);
        center.add(scroll, BorderLayout.CENTER);
        detailLabel = new JLabel("Select a month to view details.");
        detailLabel.setFont(FONT);
        detailLabel.setForeground(AppColors.TEXT);
        center.add(detailLabel, BorderLayout.SOUTH);
        return center;
    }

    private void refreshReport() {
        tableModel.setRowCount(0);
        try {
            HashMap<String, Double> incomes = incomeService.getMonthlyIncomeMap(user.getUserId());
            HashMap<String, Double> extras = incomeService.getMonthlyExtraIncomeMap(user.getUserId());
            HashMap<String, Double> spent = expenseService.getMonthlySpentMap(user.getUserId());
            Set<String> months = new TreeSet<>();
            months.addAll(incomes.keySet());
            months.addAll(extras.keySet());
            months.addAll(spent.keySet());
            // Show newest first.
            List<String> monthList = new ArrayList<>(months);
            java.util.Collections.sort(monthList);
            java.util.Collections.reverse(monthList);
            for (String month : monthList) {
                double inc = incomes.containsKey(month) ? incomes.get(month) : 0;
                double ext = extras.containsKey(month) ? extras.get(month) : 0;
                double sp = spent.containsKey(month) ? spent.get(month) : 0;
                double budget = expenseService.getBudget(user.getUserId(), month);
                double usage = (budget > 0) ? (sp / budget) * 100.0 : 0;
                tableModel.addRow(new Object[] {
                        month,
                        String.format("%.2f", inc),
                        String.format("%.2f", ext),
                        String.format("%.2f", sp),
                        String.format("%.2f", inc + ext - sp),
                        String.format("%.2f", budget),
                        String.format("%.2f", usage)
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }
}