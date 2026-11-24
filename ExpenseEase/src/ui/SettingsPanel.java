package ui;

import exception.InvalidInputException;
import model.Category;
import model.User;
import service.CategoryService;
import service.ExpenseService;
import service.UserService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.ArrayList;

// Settings screen for account, categories and old data.
public class SettingsPanel extends BasePanel {

    private MainFrame frame;
    private User user;
    private UserService userService = new UserService();
    private CategoryService categoryService = new CategoryService();
    private ExpenseService expenseService = new ExpenseService();

    private JTextField emailField;
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private DefaultTableModel categoryModel;
    private JTable categoryTable;

    public SettingsPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        add(createTopBar(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public void resetForm() {
        emailField.setText(user.getEmail());
        oldPasswordField.setText("");
        newPasswordField.setText("");
        loadCategories();
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(AppColors.HEADER);
        bar.setBorder(new EmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel("Settings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(AppColors.TEXT);
        bar.add(title, BorderLayout.WEST);
        JButton back = createButton("Back to Dashboard", AppColors.PRIMARY_DARK);
        back.addActionListener(e -> frame.showDashboard(user));
        bar.add(back, BorderLayout.EAST);
        return bar;
    }

    private JPanel createContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(AppColors.PAGE);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.add(createAccountCard());
        content.add(Box.createRigidArea(new Dimension(0, 15)));
        content.add(createCategoryCard());
        content.add(Box.createRigidArea(new Dimension(0, 15)));
        content.add(createDataCard());
        return content;
    }

    private JPanel createAccountCard() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(createTitle("Account Settings"));
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        emailField = createTextField();
        oldPasswordField = createPasswordField();
        newPasswordField = createPasswordField();
        addField(card, "Email", emailField);
        JButton updateEmailButton = createButton("Update Email", AppColors.PRIMARY);
        updateEmailButton.addActionListener(e -> updateEmail());
        card.add(updateEmailButton);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        addField(card, "Old Password", oldPasswordField);
        addField(card, "New Password", newPasswordField);
        JButton changePasswordButton = createButton("Change Password", AppColors.PRIMARY);
        changePasswordButton.addActionListener(e -> changePassword());
        card.add(changePasswordButton);
        return card;
    }

    private JPanel createCategoryCard() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(createTitle("Custom Categories"));
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        String[] columns = {"ID", "Name"};
        categoryModel = new DefaultTableModel(columns, 0);
        categoryTable = new JTable(categoryModel);
        categoryTable.setDefaultEditor(Object.class, null);
        JScrollPane scroll = new JScrollPane(categoryTable);
        scroll.setPreferredSize(new Dimension(400, 180));
        card.add(scroll);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        bar.setBackground(AppColors.CARD);
        JButton addButton = createButton("Add Category", AppColors.PRIMARY);
        JButton deleteButton = createButton("Delete Category", AppColors.DANGER);
        addButton.addActionListener(e -> addCategory());
        deleteButton.addActionListener(e -> deleteCategory());
        bar.add(addButton);
        bar.add(deleteButton);
        card.add(bar);
        return card;
    }

    private JPanel createDataCard() {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(createTitle("Data Retention"));
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        JButton deleteOldButton = createButton("Delete Data Older Than 6 Months",
                AppColors.DANGER);
        deleteOldButton.addActionListener(e -> deleteOldData());
        card.add(deleteOldButton);
        return card;
    }

    private void updateEmail() {
        try {
            userService.updateEmail(user.getUserId(), emailField.getText().trim());
            user.setEmail(emailField.getText().trim());
            JOptionPane.showMessageDialog(this, "Email updated.");
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void changePassword() {
        try {
            String oldPassword = new String(oldPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            userService.changePassword(user.getUserId(), oldPassword, newPassword);
            JOptionPane.showMessageDialog(this, "Password changed.");
            oldPasswordField.setText("");
            newPasswordField.setText("");
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCategories() {
        categoryModel.setRowCount(0);
        try {
            for (Category category : categoryService.listCategories(user.getUserId())) {
                if (!category.isDefault()) {
                    categoryModel.addRow(new Object[] {
                            category.getCategoryId(), category.getCategoryName()
                    });
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
        }
    }

    private void addCategory() {
        String name = JOptionPane.showInputDialog(this, "New category name:");
        if (name == null) {
            return;
        }
        try {
            categoryService.addCustomCategory(user.getUserId(), name);
            loadCategories();
            JOptionPane.showMessageDialog(this, "Category added.");
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteCategory() {
        int row = categoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a category first.");
            return;
        }
        int id = (int) categoryModel.getValueAt(row, 0);
        int answer = JOptionPane.showConfirmDialog(this, "Delete this category?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            categoryService.deleteCustomCategory(user.getUserId(), id);
            loadCategories();
            JOptionPane.showMessageDialog(this, "Category deleted.");
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteOldData() {
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete data older than 6 months?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            expenseService.deleteOldData(user.getUserId());
            JOptionPane.showMessageDialog(this, "Old data deleted.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}