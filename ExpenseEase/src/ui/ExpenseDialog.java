package ui;

import exception.InvalidInputException;
import model.Category;
import model.Expense;
import model.User;
import service.CategoryService;
import service.ExpenseService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

// A small dialog used for both adding and editing an expense.
public class ExpenseDialog extends JDialog implements ActionListener {

    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private DashboardPanel dashboard;
    private User user;
    private Expense oldExpense;
    private CategoryService categoryService = new CategoryService();
    private ExpenseService expenseService = new ExpenseService();
    private JComboBox<Category> categoryBox;
    private JTextField amountField;
    private JTextField dateField;
    private JTextField descriptionField;
    private JButton saveButton;
    private JButton cancelButton;

    // Builds the dialog. oldExpense is null when adding.
    public ExpenseDialog(MainFrame frame, DashboardPanel dashboard,
                         User user, Expense oldExpense) {
        super(frame, true);
        this.dashboard = dashboard;
        this.user = user;
        this.oldExpense = oldExpense;
        setTitle(oldExpense == null ? "Add Expense" : "Edit Expense");
        setSize(420, 420);
        setLocationRelativeTo(frame);
        setLayout(new BorderLayout());
        add(createForm(), BorderLayout.CENTER);
        add(createButtons(), BorderLayout.SOUTH);
    }

    // Creates the input fields of the form.
    private JPanel createForm() {
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(AppColors.CARD);
        form.setBorder(new EmptyBorder(20, 25, 5, 25));
        categoryBox = new JComboBox<>();
        categoryBox.setFont(FONT);
        categoryBox.setMaximumSize(new Dimension(320, 32));
        loadCategories();
        amountField = createField();
        dateField = createField();
        descriptionField = createField();
        dateField.setText(getToday());
        addRow(form, "Category", categoryBox);
        addRow(form, "Amount", amountField);
        addRow(form, "Date (yyyy-MM-dd)", dateField);
        addRow(form, "Description", descriptionField);
        if (oldExpense != null) {
            fillOldValues();
        }
        return form;
    }

    // Fills the form when the user edits an old expense.
    private void fillOldValues() {
        amountField.setText(String.valueOf(oldExpense.getAmount()));
        dateField.setText(oldExpense.getDate());
        descriptionField.setText(oldExpense.getDescription());
        for (int i = 0; i < categoryBox.getItemCount(); i++) {
            Category category = categoryBox.getItemAt(i);
            if (category.getCategoryId() == oldExpense.getCategoryId()) {
                categoryBox.setSelectedIndex(i);
                return;
            }
        }
    }

    // Loads the default and custom categories into the dropdown.
    private void loadCategories() {
        try {
            ArrayList<Category> list = categoryService.listCategories(user.getUserId());
            for (Category category : list) {
                categoryBox.addItem(category);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Creates one text field for the form.
    private JTextField createField() {
        JTextField field = new JTextField();
        field.setFont(FONT);
        field.setMaximumSize(new Dimension(320, 32));
        return field;
    }

    // Adds a label and a field to the form.
    private void addRow(JPanel form, String text, Component field) {
        JLabel label = new JLabel(text);
        label.setFont(FONT);
        label.setForeground(AppColors.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(320, 32));
        form.add(label);
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(field);
        form.add(Box.createRigidArea(new Dimension(0, 12)));
    }

    // Creates the Save and Cancel buttons.
    private JPanel createButtons() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bar.setBackground(AppColors.CARD);
        cancelButton = createSmallButton("Cancel", AppColors.PRIMARY_DARK);
        saveButton = createSmallButton("Save", AppColors.PRIMARY);
        cancelButton.addActionListener(this);
        saveButton.addActionListener(this);
        bar.add(cancelButton);
        bar.add(saveButton);
        return bar;
    }

    // Creates one flat button for the dialog.
    private JButton createSmallButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(FONT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 18, 8, 18));
        return button;
    }

    // OOP: Overriding actionPerformed from ActionListener.
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveButton) {
            saveExpense();
        } else if (e.getSource() == cancelButton) {
            dispose();
        }
    }

    // Adds or updates the expense and refreshes the dashboard.
    private void saveExpense() {
        Category category = (Category) categoryBox.getSelectedItem();
        if (category == null) {
            JOptionPane.showMessageDialog(this, "Please select a category.");
            return;
        }
        String date = dateField.getText().trim();
        String description = descriptionField.getText().trim();
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            if (oldExpense == null) {
                expenseService.addExpense(user.getUserId(),
                        category.getCategoryId(), amount, date, description);
            } else {
                expenseService.updateExpense(oldExpense.getExpenseId(),
                        user.getUserId(), category.getCategoryId(), amount,
                        date, description);
            }
            dashboard.refreshDashboard();
            dispose();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Amount must be a number.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Returns today's date as yyyy-MM-dd.
    private String getToday() {
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        return today.toString();
    }
}
