package ui;

import exception.InvalidInputException;
import model.Category;
import model.RecurringTransaction;
import model.User;
import service.CategoryService;
import service.RecurringService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;

// Manages recurring salary, expenses and EMI.
public class RecurringManagerDialog extends JDialog implements ActionListener {

    private static final Font FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private User user;
    private RecurringService recurringService = new RecurringService();
    private CategoryService categoryService = new CategoryService();
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton addButton;
    private JButton editButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton cancelButton;
    private JButton closeButton;

    public RecurringManagerDialog(MainFrame frame, User user) {
        super(frame, "Recurring Transactions", true);
        this.user = user;
        setSize(900, 500);
        setLocationRelativeTo(frame);
        setLayout(new BorderLayout());
        add(createTablePanel(), BorderLayout.CENTER);
        add(createButtonBar(), BorderLayout.SOUTH);
        refreshTable();
    }

    private JScrollPane createTablePanel() {
        String[] columns = {"ID", "Type", "Title", "Category", "Amount",
                "Frequency", "Next Due", "Active", "Payments"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setFont(FONT);
        table.setRowHeight(26);
        table.setDefaultEditor(Object.class, null);
        JScrollPane scroll = new JScrollPane(table);
        return scroll;
    }

    private JPanel createButtonBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bar.setBackground(AppColors.PAGE);
        addButton = createButton("Add", AppColors.PRIMARY);
        editButton = createButton("Edit", AppColors.PRIMARY);
        pauseButton = createButton("Pause", AppColors.PRIMARY_DARK);
        resumeButton = createButton("Resume", AppColors.PRIMARY_DARK);
        cancelButton = createButton("Cancel", AppColors.DANGER);
        closeButton = createButton("Close", AppColors.PRIMARY_DARK);
        JButton[] buttons = {addButton, editButton, pauseButton, resumeButton,
                cancelButton, closeButton};
        for (JButton button : buttons) {
            button.addActionListener(this);
            bar.add(button);
        }
        return bar;
    }

    protected JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(FONT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(9, 18, 9, 18));
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addRecurring();
        } else if (e.getSource() == editButton) {
            editRecurring();
        } else if (e.getSource() == pauseButton) {
            changeActive(false);
        } else if (e.getSource() == resumeButton) {
            changeActive(true);
        } else if (e.getSource() == cancelButton) {
            cancelRecurring();
        } else if (e.getSource() == closeButton) {
            dispose();
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        try {
            for (RecurringTransaction rec : recurringService.list(user.getUserId())) {
                String payments = "";
                if ("EMI".equals(rec.getTransactionType())) {
                    payments = rec.getPaymentsDone() + " / " + rec.getPaymentsTotal();
                }
                tableModel.addRow(new Object[] {
                        rec.getRecurringId(), rec.getTransactionType(), rec.getTitle(),
                        rec.getCategoryName(), String.format("%.2f", rec.getAmount()),
                        "Every " + rec.getFrequencyMonths() + " month(s)",
                        rec.getNextDueDate(), rec.isActive() ? "Yes" : "No", payments
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void addRecurring() {
        RecurringFormDialog dialog = new RecurringFormDialog(this, user, null);
        dialog.setVisible(true);
        refreshTable();
    }

    private void editRecurring() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            RecurringTransaction old = null;
            for (RecurringTransaction rec : recurringService.list(user.getUserId())) {
                if (rec.getRecurringId() == id) {
                    old = rec;
                    break;
                }
            }
            if (old != null) {
                RecurringFormDialog dialog = new RecurringFormDialog(this, user, old);
                dialog.setVisible(true);
                refreshTable();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void changeActive(boolean active) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            recurringService.setActive(id, user.getUserId(), active);
            refreshTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void cancelRecurring() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int answer = JOptionPane.showConfirmDialog(this, "Cancel this recurring transaction?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            recurringService.cancel(id, user.getUserId());
            refreshTable();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    // Small form dialog for add/edit recurring.
    private class RecurringFormDialog extends JDialog implements ActionListener {
        private RecurringTransaction old;
        private JComboBox<String> typeBox;
        private JComboBox<Category> categoryBox;
        private JTextField titleField;
        private JTextField amountField;
        private JTextField frequencyField;
        private JTextField dateField;
        private JTextField paymentsField;
        private JButton dateButton;
        private JButton saveButton;
        private JButton cancelButton;

        RecurringFormDialog(java.awt.Component parent, User user, RecurringTransaction old) {
            super(RecurringManagerDialog.this, true);
            this.old = old;
            setTitle(old == null ? "Add Recurring" : "Edit Recurring");
            setSize(420, 470);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            JPanel form = new JPanel();
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.setBackground(AppColors.CARD);
            form.setBorder(new EmptyBorder(20, 25, 5, 25));

            typeBox = new JComboBox<>(new String[]{"INCOME", "EXPENSE", "EMI"});
            categoryBox = new JComboBox<>();
            loadCategories();
            titleField = createField();
            amountField = createField();
            frequencyField = createField();
            frequencyField.setText("1");
            dateField = createField();
            dateField.setText(getToday());
            paymentsField = createField();
            paymentsField.setText("12");

            addRow(form, "Type", typeBox);
            addRow(form, "Title", titleField);
            addRow(form, "Category", categoryBox);
            addRow(form, "Amount", amountField);
            addRow(form, "Frequency (months)", frequencyField);
            addRow(form, "Start / Next due date", dateField);
            dateButton = new JButton("Pick Date");
            dateButton.setFont(FONT);
            dateButton.addActionListener(this);
            form.add(dateButton);
            form.add(Box.createRigidArea(new Dimension(0, 12)));
            addRow(form, "Payments (EMI only)", paymentsField);

            if (old != null) {
                typeBox.setSelectedItem(old.getTransactionType());
                typeBox.setEnabled(false);
                titleField.setText(old.getTitle());
                amountField.setText(String.valueOf(old.getAmount()));
                frequencyField.setText(String.valueOf(old.getFrequencyMonths()));
                dateField.setText(old.getNextDueDate());
                paymentsField.setText(String.valueOf(old.getPaymentsTotal()));
                for (int i = 0; i < categoryBox.getItemCount(); i++) {
                    Category c = categoryBox.getItemAt(i);
                    if (c.getCategoryId() == old.getCategoryId()) {
                        categoryBox.setSelectedIndex(i);
                    }
                }
            }

            add(form, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            buttons.setBackground(AppColors.CARD);
            cancelButton = createSmallButton("Cancel", AppColors.PRIMARY_DARK);
            saveButton = createSmallButton("Save", AppColors.PRIMARY);
            cancelButton.addActionListener(this);
            saveButton.addActionListener(this);
            buttons.add(cancelButton);
            buttons.add(saveButton);
            add(buttons, BorderLayout.SOUTH);
        }

        private void loadCategories() {
            try {
                for (Category category : categoryService.listCategories(user.getUserId())) {
                    categoryBox.addItem(category);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            }
        }

        private JTextField createField() {
            JTextField field = new JTextField();
            field.setFont(FONT);
            field.setMaximumSize(new Dimension(320, 32));
            return field;
        }

        private void addRow(JPanel form, String text, java.awt.Component field) {
            JLabel label = new JLabel(text);
            label.setFont(FONT);
            label.setForeground(AppColors.TEXT);
            label.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
            field.setMaximumSize(new Dimension(320, 32));
            form.add(label);
            form.add(Box.createRigidArea(new Dimension(0, 4)));
            form.add(field);
            form.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        private JButton createSmallButton(String text, Color color) {
            JButton button = new JButton(text);
            button.setFont(FONT);
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            button.setBorder(new EmptyBorder(8, 18, 8, 18));
            return button;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == saveButton) {
                save();
            } else if (e.getSource() == cancelButton) {
                dispose();
            } else if (e.getSource() == dateButton) {
                String date = CalendarDialog.showDialog(this);
                if (date != null) {
                    dateField.setText(date);
                }
            }
        }

        private void save() {
            String type = (String) typeBox.getSelectedItem();
            Category category = (Category) categoryBox.getSelectedItem();
            int categoryId = (category == null) ? 0 : category.getCategoryId();
            String title = titleField.getText().trim();
            String date = dateField.getText().trim();
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                int frequency = Integer.parseInt(frequencyField.getText().trim());
                int payments = 0;
                if ("EMI".equals(type)) {
                    payments = Integer.parseInt(paymentsField.getText().trim());
                }
                if (old == null) {
                    recurringService.add(user.getUserId(), type, title, categoryId,
                            amount, frequency, date, payments);
                } else {
                    recurringService.update(old.getRecurringId(), user.getUserId(),
                            title, categoryId, amount, frequency, date);
                }
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } catch (InvalidInputException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Invalid input", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private String getToday() {
            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            return today.toString();
        }
    }
}