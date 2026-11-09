package ui;

import exception.InvalidInputException;
import exception.LoginFailedException;
import service.UserService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

// OOP: Inheritance - ForgotPasswordPanel extends BasePanel.
// OOP: Interface implementation - ActionListener.
public class ForgotPasswordPanel extends BasePanel implements ActionListener {

    private MainFrame frame;
    private UserService userService = new UserService();
    private JTextField usernameField;
    private JTextField emailField;
    private JTextField answerField;
    private JPasswordField newPasswordField;
    private JButton resetButton;
    private JButton backButton;

    // Builds the forgot password screen.
    public ForgotPasswordPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(createTitle("Forgot Password"));
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(createLabel("Answer the security question to reset"));
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        usernameField = createTextField();
        emailField = createTextField();
        answerField = createTextField();
        newPasswordField = createPasswordField();
        addField(card, "Username", usernameField);
        addField(card, "Email", emailField);
        addField(card, "Security answer", answerField);
        addField(card, "New password", newPasswordField);
        resetButton = createButton("Reset Password", AppColors.PRIMARY);
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.setMaximumSize(new Dimension(280, 40));
        resetButton.addActionListener(this);
        card.add(resetButton);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        backButton = createLinkButton("Back to login");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(this);
        card.add(backButton);
        add(card);
    }

    // OOP: Overriding the abstract method from BasePanel.
    @Override
    public void resetForm() {
        usernameField.setText("");
        emailField.setText("");
        answerField.setText("");
        newPasswordField.setText("");
        usernameField.requestFocusInWindow();
    }

    // OOP: Overriding actionPerformed from ActionListener.
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == resetButton) {
            doReset();
        } else if (e.getSource() == backButton) {
            frame.showLogin();
        }
    }

    // Checks the answer and saves the new password.
    private void doReset() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String answer = answerField.getText().trim();
        String newPassword = new String(newPasswordField.getPassword());
        try {
            userService.forgotPassword(username, email, answer, newPassword);
            JOptionPane.showMessageDialog(this,
                    "Password changed. Please login with the new password.");
            frame.showLogin();
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (LoginFailedException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Reset failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
