package ui;

import exception.LoginFailedException;
import model.User;
import service.UserService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

// OOP: Inheritance - LoginPanel extends BasePanel.
// OOP: Interface implementation - ActionListener.
public class LoginPanel extends BasePanel implements ActionListener {

    private MainFrame frame;
    private UserService userService = new UserService();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton forgotButton;

    // Builds the login screen.
    public LoginPanel(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.add(createTitle("ExpenseEase"));
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(createLabel("Login to continue"));
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        usernameField = createTextField();
        passwordField = createPasswordField();
        addField(card, "Username", usernameField);
        addField(card, "Password", passwordField);
        loginButton = createButton("Login", AppColors.PRIMARY);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(280, 40));
        loginButton.addActionListener(this);
        card.add(loginButton);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        links.setBackground(AppColors.CARD);
        registerButton = createLinkButton("Create account");
        forgotButton = createLinkButton("Forgot password?");
        registerButton.addActionListener(this);
        forgotButton.addActionListener(this);
        links.add(registerButton);
        links.add(forgotButton);
        links.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(links);
        add(card);
    }

    // OOP: Overriding the abstract method from BasePanel.
    @Override
    public void resetForm() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocusInWindow();
    }

    // OOP: Overriding actionPerformed from ActionListener.
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            doLogin();
        } else if (e.getSource() == registerButton) {
            frame.showRegister();
        } else if (e.getSource() == forgotButton) {
            frame.showForgot();
        }
    }

    // Checks the login and opens the dashboard.
    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.");
            return;
        }
        try {
            User user = userService.login(username, password);
            passwordField.setText("");
            frame.showDashboard(user);
        } catch (LoginFailedException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Login failed", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
