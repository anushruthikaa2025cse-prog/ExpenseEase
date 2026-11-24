package ui;

import exception.InvalidInputException;
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

// OOP: Inheritance - RegisterPanel extends BasePanel.
// OOP: Interface implementation - ActionListener.
public class RegisterPanel extends BasePanel implements ActionListener {

    private MainFrame frame;
    private UserService userService = new UserService();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField emailField;
    private JTextField answerField;
    private JButton registerButton;
    private JButton backButton;

    // Builds the register screen.
    public RegisterPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(createTitle("Create Account"));
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(createLabel("All fields are required"));
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        usernameField = createTextField();
        passwordField = createPasswordField();
        emailField = createTextField();
        answerField = createTextField();
        addField(card, "Username", usernameField);
        addField(card, "Password", passwordField);
        addField(card, "Email", emailField);
        addField(card, "Security answer (your pet's name)", answerField);
        registerButton = createButton("Register", AppColors.PRIMARY);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(280, 40));
        registerButton.addActionListener(this);
        card.add(registerButton);
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
        passwordField.setText("");
        emailField.setText("");
        answerField.setText("");
        usernameField.requestFocusInWindow();
    }

    // OOP: Overriding actionPerformed from ActionListener.
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == registerButton) {
            doRegister();
        } else if (e.getSource() == backButton) {
            frame.showLogin();
        }
    }

    // Saves the new user and returns to the login screen.
    private void doRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String email = emailField.getText().trim();
        String answer = answerField.getText().trim();
        try {
            userService.register(username, password, email, answer);
            JOptionPane.showMessageDialog(this,
                    "Registration successful. Please login.");
            frame.showLogin();
        } catch (InvalidInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Invalid input", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
